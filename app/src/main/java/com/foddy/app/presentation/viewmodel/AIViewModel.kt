package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.ChatMessage
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.MessageRole
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.repository.AIRepository
import com.foddy.app.domain.repository.MenuRepository
import com.foddy.app.core.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.FlowPreview

data class AIChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("Xin chào! Tôi là trợ lý AI của Foddy. Tôi có thể giúp bạn tìm món ăn, theo dõi đơn hàng hoặc gợi ý nhà hàng ngon nhất.", MessageRole.MODEL)
    ),
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val suggestedPrompts: List<String> = listOf("Gợi ý món ăn trưa nay", "Tìm món bún đậu mắm tôm", "Đơn hàng của tôi đâu rồi?", "Món nào đang giảm giá?")
)

@OptIn(FlowPreview::class)
@HiltViewModel
class AIViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _chatUiState = MutableStateFlow(AIChatUiState())
    val chatUiState: StateFlow<AIChatUiState> = _chatUiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val searchResults: StateFlow<List<FoodItem>> = _searchResults.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        searchQuery
            .debounce(500)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _searchResults.value = emptyList()
                } else {
                    performSemanticSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, MessageRole.USER)
        _chatUiState.update { it.copy(
            messages = it.messages + userMessage,
            isTyping = true
        ) }

        viewModelScope.launch {
            val responseFlow = aiRepository.getChatResponse(_chatUiState.value.messages, text)
            var fullResponse = ""
            
            _chatUiState.update { it.copy(messages = it.messages + ChatMessage("", MessageRole.MODEL)) }

            responseFlow.collect { chunk ->
                fullResponse += chunk
                _chatUiState.update { state ->
                    val lastMsg = state.messages.last().copy(content = fullResponse)
                    state.copy(
                        messages = state.messages.dropLast(1) + lastMsg
                    )
                }
            }
            _chatUiState.update { it.copy(isTyping = false) }
        }
    }

    fun semanticSearch(query: String) {
        searchQuery.value = query
    }

    private fun performSemanticSearch(query: String) {
        viewModelScope.launch {
            _chatUiState.update { it.copy(isLoading = true) }
            
            menuRepository.getMenuItemsWithCache().first { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val result = aiRepository.semanticSearch(query, resource.data)
                    result.onSuccess { items ->
                        _searchResults.value = items
                    }.onFailure {
                        // Handle error
                    }
                    true
                } else if (resource is Resource.Error) {
                    true
                } else {
                    false
                }
            }
            _chatUiState.update { it.copy(isLoading = false) }
        }
    }

    private val _businessInsights = MutableStateFlow<String?>(null)
    val businessInsights: StateFlow<String?> = _businessInsights.asStateFlow()

    private val _isAnalyzingBusiness = MutableStateFlow(false)
    val isAnalyzingBusiness: StateFlow<Boolean> = _isAnalyzingBusiness.asStateFlow()

    fun getBusinessInsights(orders: List<OrderRequest>) {
        if (orders.isEmpty()) return
        
        viewModelScope.launch {
            _isAnalyzingBusiness.value = true
            val orderData = orders.joinToString("\n") { 
                "Order: ${it.id}, Amount: ${it.totalPrice}, Status: ${it.status}, Items: ${it.items.joinToString { i -> i.foodItem.name }}" 
            }
            val result = aiRepository.getBusinessInsights(orderData)
            result.onSuccess { insights ->
                _businessInsights.value = insights
            }.onFailure {
                _businessInsights.value = "Lỗi khi phân tích dữ liệu kinh doanh."
            }
            _isAnalyzingBusiness.value = false
        }
    }
}
