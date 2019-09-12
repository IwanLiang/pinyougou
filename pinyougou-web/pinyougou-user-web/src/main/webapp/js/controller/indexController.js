// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName :  ''// 用户数据
        },
        methods : { // 操作方法
            // 获取登录用户名
            loadUsername (){
                // 发送异步请求
                axios.get("/user/showName").then(response => {
                    // 获取响应数据
                    this.loginName = response.data.loginName;
                });
            }
        },
        created : function(){
            // 获取登录用户名
            this.loadUsername();
        }
    });
};