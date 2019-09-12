window.onload = function () {
    // 创建Vue对象
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '' // 登录用户名
        },
        methods : { // 操作方法
            // 获取登录用户名
            findLoginName : function () {
                // 发送异步请求
                axios.get("/user/findLoginName").then(function(response){
                    // 获取响应数据
                    vue.loginName = response.data.loginName;
                });
            }
        },
        created : function () { // 创建生命周期
            this.findLoginName();
        }
    });
}