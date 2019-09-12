// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '', // 登录用户名
            redirectUrl : '', // 重定向URL
            outTradeNo : '', // 订单交易号
            money : 0 // 交易总金额
        },
        methods : { // 操作方法
            // 加载用户
            loadUsername : function () {
                // 定义重定向URL
                this.redirectUrl = window.encodeURIComponent(location.href);
                // 获取登录用户名
                axios.get("/user/showName").then(function(response){
                    vue.loginName = response.data.loginName;
                });
            },
            // 生成微信支付二维码
            genPayCode : function () {
                axios.get("/order/genPayCode").then(function(response){
                    /** 获取金额(转化成元) */
                    vue.money = (response.data.totalFee / 100).toFixed(2);
                    /** 获取订单交易号 */
                    vue.outTradeNo= response.data.outTradeNo;
                    /** 生成二维码 */
                    var qr = new QRious({
                        element : document.getElementById('qrious'),
                        size : 250,
                        level : 'H',
                        value : response.data.codeUrl
                    });

                    /**
                     * 开启定时器
                     * 第一个参数：调用的函数
                     * 第二个参数：时间毫秒数(3000毫秒也就是3秒)
                     * */
                    var timer = setInterval(function() {
                        /** 发送请求，查询支付状态 */
                        axios.get("/order/queryPayStatus?outTradeNo="
                            + vue.outTradeNo).then(function(response){
                            if(response.data.status == 1){// 支付成功
                                /** 取消定时器 */
                                window.clearInterval(timer);
                                location.href = "/order/paysuccess.html?money=" + vue.money;
                            }
                            if(response.data.status == 3){// 支付失败
                                /** 取消定时器 */
                                window.clearInterval(timer);
                                location.href = "/order/payfail.html";
                            }
                        });
                    }, 3000);
                });
            }
        },
        created : function () { // 创建生命周期
            this.loadUsername();
            this.genPayCode();
        }
    });
};