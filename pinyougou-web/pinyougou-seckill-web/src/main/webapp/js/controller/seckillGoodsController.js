// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '', // 登录用户名
            redirectUrl : '', // 重定向URL
            seckillGoodsList : [] // 秒杀商品数组
        },
        methods : { // 操作方法
            // 加载用户
            loadUsername () {
                // 定义重定向URL
                this.redirectUrl = window.encodeURIComponent(location.href);
                // 获取登录用户名
                axios.get("/user/showName").then(function(response){
                    vue.loginName = response.data.loginName;
                });
            },
            // 查询秒杀商品
            findSeckillGoods (){
                // 发送异步请求
                axios.get("/seckill/findSeckillGoods").then(response =>{
                    // 获取响应数据
                    this.seckillGoodsList= response.data;
                });
            }
        },
        created : function () { // 创建生命周期
           this.loadUsername();
           this.findSeckillGoods();
        }
    });
};