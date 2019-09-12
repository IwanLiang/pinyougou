// 窗口加载完
window.onload = function () {
    // 创建Vue对象
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            dataList : [], // 定义数组，接收后台响应数据
            entity : {}, // 数据封装对象(表单)
            page : 1, // 当前页码
            pages : 0, // 总页数
            searchEntity : {}, // 搜索条件数据封装
            ids : [], // 复选框选中的id数组
            checked : false // 全选复选框是否选中
        },
        methods : { // 定义操作方法
            search (page){ // 搜索方法
                // 发送异步请求
                axios.get("/goods/findByPage?page=" + page,
                    {params : this.searchEntity})
                    .then(function(response){
                        // 获取响应数据
                        vue.dataList = response.data.rows;
                        // 设置总页数
                        vue.pages = response.data.pages;
                        // 设置当前页码
                        vue.page = page;
                        // 设置ids数组为空
                        vue.ids = [];
                    });
            },
            // 修改商品审核的状态码
            updateStatus (status){
                if (this.ids.length > 0) {
                    // 发送异步请求
                    axios.get("/goods/updateStatus?ids="
                        + this.ids + "&status=" + status).then(response => {
                         // 获取响应数据
                         if (response.data){
                             // 重新查询商品
                             this.search(this.page);
                         }else{
                             alert("操作失败！");
                         }
                    });
                }else{
                    alert("请选择要审核的商品！");
                }
            },
            checkAll (e) { // 全选复选框
                this.ids = []; // 先清空数组
                if (e.target.checked){ // 判断复选框是否选中
                    for (var i = 0; i < this.dataList.length; i++){
                        this.ids.push(this.dataList[i].id);
                    }
                }
            },
            del () { // 删除
                if (this.ids.length > 0){
                    axios.get("/goods/delete?ids="
                        + this.ids).then(response => {
                        if (response.data){
                            // 计算当前页码(如果删除为最后一页查询上一页)
                            var page = this.page == this.pages && this.checked
                                ? this.page - 1 : this.page;
                            // 重新加载数据
                            this.search(page);
                        }else{
                            alert("删除失败！");
                        }
                    });
                }else {
                    alert("请选择要删除的记录！");
                }
            }
        },
        created () { // 创建生命周期(初始化方法)
            // 调用搜索方法
            this.search(this.page);
        },
        updated () { // 更新数据生命周期
            // 检查全选checkbox是否选中
            if (this.dataList.length > 0) {
                this.checked = (this.ids.length == this.dataList.length);
            }else {
                this.checked = false;
            }
        }
    });
};