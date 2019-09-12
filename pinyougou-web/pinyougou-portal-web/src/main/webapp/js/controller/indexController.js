// 窗口加载完
window.onload = function () {
    new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            contentList : [], // 大广告数据
            keywords : '', // 搜索关键字
            loginName : '', // 登录用户名
            redirectUrl : '' // 重定向URL
        },
        methods : { // 操作方法
            // 根据分类id查询大广告数据
            findContentByCategoryId (categoryId){
                // 发送异步请求
                axios.get("/content/findContentByCategoryId?categoryId="
                    + categoryId).then(response => {
                    // 获取响应数据
                    this.contentList = response.data;
                });
            },
            // 搜索方法
            search (){
                // 跳转到搜索系统
                location.href = "http://search.pinyougou.com/?keywords=" + this.keywords;
            },
            // 加载登录用户名
            loadUsername (){
                // 重定向URL进行uri编码
                this.redirectUrl = window.encodeURIComponent(location.href);
                // 发送异步请求
                axios.get("/user/showName").then(response => {
                    // 获取响应数据
                    this.loginName = response.data.loginName;
                });
            }
        },
        created () { // 创建生命周期
            this.findContentByCategoryId(1);
            this.loadUsername();
        }
    });
};