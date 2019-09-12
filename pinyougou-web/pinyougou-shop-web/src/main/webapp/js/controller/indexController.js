// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '' // 登录用户名
        },
        methods : { // 操作方法
            findLoginName : function () { // 获取登录用户名
                // 发送异步请示
                axios.get("/findLoginName")
                    .then(function(response){
                    // 获取响应数据
                    vue.loginName = response.data.loginName;
                });
            }
        },
        created : function () { // 创建生命周期
            // 调用获取登录用户名方法
            this.findLoginName();
        }
    });
};