rules_version = '2';
service cloud.firestore {match /databases/{database}/documents {
    
    // Hàm bổ trợ: Kiểm tra người dùng đã đăng nhập chưa
    function isSignedIn() {
      return request.auth != null;
    }

    // Quy tắc cho Danh mục (Nhà hàng, Món ăn, Thể loại)
    // Cho phép mọi người đọc để hiển thị App, chỉ người dùng đã đăng nhập mới được ghi (Seed data)
    match /restaurants/{restaurantId} {
      allow read: if true;
      allow write: if isSignedIn(); 
    }
    
    match /foods/{foodId} {
      allow read: if true;
      allow write: if isSignedIn();
    }
    
    match /categories/{categoryId} {
      allow read: if true;
      allow write: if isSignedIn();
    }

    // Quy tắc cho Đơn hàng (Orders) - Rất quan trọng cho Tài xế
    match /orders/{orderId} {
      // Cho phép đọc nếu đã đăng nhập (để tài xế tìm đơn mới và khách xem đơn của mình)
      allow read: if isSignedIn();
      // Cho phép tạo đơn hàng mới
      allow create: if isSignedIn();
      // Cho phép cập nhật trạng thái đơn (Tài xế nhận đơn, Quán làm xong, v.v.)
      allow update: if isSignedIn();
    }

    // Vị trí tài xế
    match /driver_locations/{orderId} {
      allow read, write: if isSignedIn();
    }

    // Tin nhắn Chat
    match /chats/{chatId} {
      allow read, create: if isSignedIn();
    }

    // Thông tin người dùng và tài xế
    match /users/{userId} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && request.auth.uid == userId;
    }
    
    match /drivers/{driverId} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && request.auth.uid == driverId;
    }

    // Địa chỉ và Yêu thích
    match /addresses/{addressId} {
      allow read, write: if isSignedIn();
    }
    
    match /favorites/{favoriteId} {
      allow read, write: if isSignedIn();
    }

    // Đánh giá và Thông báo
    match /reviews/{reviewId} {
      allow read: if true;
      allow create, update: if isSignedIn();
    }
    
    match /notifications/{notificationId} {
      allow read, write: if isSignedIn();
    }

    // Mặc định cho các collection khác: Yêu cầu đăng nhập
    match /{document=**} {
      allow read, write: if isSignedIn();
    }
  }
}