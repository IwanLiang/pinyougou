// 窗口加载完
window.onload = function () {
    // 创建Vue对象
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName:'' , //获取登录用户名
            entity : {} // 商家数据封装对象(表单)
        },
        methods : { // 定义操作方法
            //查询商家id
            findLoginName : function () { // 获取登录用户名
                // 发送异步请示
                axios.get("/findLoginName")
                    .then(function(response){
                        // 获取响应数据
                        vue.loginName = response.data.loginName;
                        //设置商家对象的id
                        vue.entity.sellerId = vue.loginName;
                        //回显数据
                        vue.show(vue.loginName);
                    });
            },
            //商家数据回显
            show : function(loginName){ // 数据回显
               //发送异步请求获取商家信息
                axios.get("/seller/show?id=" + loginName).then(function (response) {
                    vue.entity = response.data;
                })
            },
            //保存商家修改的数据
            update:function (entity) {
                //异步请求保存商家修改的数据
                axios.post("/seller/update",this.entity).then(function (response) {
                    if(response == true){
                        window.location.href = "seller.html";
                    }else{
                        alert(" 操作失败")
                    }

                })
            }
        },
        created : function () { // 创建生命周期(初始化方法)
            // 页面加载回显数据
            this.findLoginName();
        },
        updated : function () { // 更新数据生命周期
        }
    });
};