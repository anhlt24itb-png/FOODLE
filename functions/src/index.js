const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.onOrderStatusChanged = functions.firestore
    .document('orders/{orderId}')
    .onUpdate(async (change, context) => {
        const newValue = change.after.data();
        const previousValue = change.before.data();
        const orderId = context.params.orderId;

        // Check if status has changed
        if (newValue.status === previousValue.status) return null;

        const userId = newValue.userId;
        const status = newValue.status;

        // Get user FCM token
        const userDoc = await admin.firestore().collection('users').document(userId).get();
        if (!userDoc.exists) {
            console.log('No such user!');
            return null;
        }

        const fcmToken = userDoc.data().fcmToken;
        if (!fcmToken) {
            console.log('User has no FCM token');
            return null;
        }

        let title = 'Foddy Update';
        let body = '';

        switch (status) {
            case 'accepted':
                body = 'Đơn hàng của bạn đã được nhà hàng chấp nhận!';
                break;
            case 'preparing':
                body = 'Nhà hàng đang chuẩn bị món ăn cho bạn.';
                break;
            case 'delivering':
                body = 'Tài xế đang giao hàng đến bạn!';
                break;
            case 'completed':
                body = 'Đơn hàng đã hoàn thành. Chúc bạn ngon miệng!';
                break;
            case 'cancelled':
                body = 'Đơn hàng của bạn đã bị hủy.';
                break;
            default:
                body = `Trạng thái đơn hàng: ${status}`;
        }

        const message = {
            notification: {
                title: title,
                body: body
            },
            data: {
                orderId: orderId,
                status: status,
                type: 'ORDER_STATUS_CHANGE'
            },
            token: fcmToken
        };

        try {
            const response = await admin.messaging().send(message);
            console.log('Successfully sent message:', response);
            return response;
        } catch (error) {
            console.log('Error sending message:', error);
            return null;
        }
    });
