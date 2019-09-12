// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '', // 登录用户名
            redirectUrl : '', // 重定向URL
            carts : [],  // 用户的购物车
            totalEntity : {totalNum : 0, totalMoney : 0}, // 总计对象
            addressList : [], // 收件地址
            address : {},// 记录用户选中的收件地址
            order : {paymentType : 1} // 订单请求参数封装对象
        },
        methods : { // 操作方法
            // 加载登录用户名
            loadUsername (){
                // 重定向URL进行uri编码
                this.redirectUrl = window.encodeURIComponent(location.href);
                // 发送异步请求
                axios.get("/user/showName").then(response => {
                    // 获取响应数据
                    this.loginName = response.data.loginName;
                });
            },
            // 查询用户的购物车
            findCart (){
                // 发送异步请求
                axios.get("/cart/findCart").then(response => {
                    // 获取响应数据 List<Cart> : [{},{}]
                    this.carts = response.data;
                    // 总计对象
                    this.totalEntity = {totalNum : 0, totalMoney : 0};

                    // 迭代用户的购物车
                    for (let cart of this.carts){
                        // 迭代商家的购物车
                        for (let orderItem of cart.orderItems){
                            // 统计购买总数量
                            this.totalEntity.totalNum += orderItem.num;
                            // 统计购买总金额
                            this.totalEntity.totalMoney += orderItem.totalFee;
                        }
                    }
                });
            },
            // 购物车加减、删除
            addCart (itemId, num) {
                // 发送异步请求
                axios.get("/cart/addCart?itemId=" + itemId
                    + "&num=" + num).then(response => {
                    if (response.data){
                        // 操作成功，重新查询用户的购物车
                        this.findCart();
                    }else {
                        alert("操作失败！");
                    }
                });
            },
            // 根据用户获取收件地址
            findAddressByUser (){
                // 发送异步请求
                axios.get("/order/findAddressByUser").then(response => {
                    // 获取响应数据[{},{}]
                    this.addressList = response.data;
                    // 取数组中的第一个
                    this.address = this.addressList[0];
                });
            },
            // 记录用户选择的地址
            selectAddress (item){
                this.address = item;
            },
            // 提交订单
            submitOrder() {
                // 封装请求参数
                // 设置收件人地址
                this.order.receiverAreaName = this.address.address;
                // 设置收件人手机号码
                this.order.receiverMobile = this.address.mobile;
                // 设置收件人
                this.order.receiver = this.address.contact;
                // 设置订单来源(pc端)
                this.order.sourceType = 2;
                // 发送异步请求
                axios.post("/order/save", this.order).then(response => {
                    // 获取响应数据
                    if (response.data){
                        // 判断支付方式
                        if (this.order.paymentType == 1){ // 在线支付
                            // 跳转到支付页面
                            location.href = "/order/pay.html";
                        }else {
                            // 货到付款
                            location.href = "/order/paysuccess.html";
                        }
                    }else {
                        alert("保存订单失败！");
                    }
                });
            }
        },
        created : function () { // 创建生命周期
            // 获取登录用户名
            this.loadUsername();
            // 查询用户的购物车
            this.findCart();
            // 查询收件地址
            this.findAddressByUser();
        }
    });
};