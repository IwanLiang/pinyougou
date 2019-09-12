window.onload = function () {
    // 创建Vue实例
    var vue = new Vue({
        el : "#app", // 元素绑定
        data : { // 数据模型
            dataList : [], // 品牌数组
            entity : {}, // 表单数据封装
            pages : 0,  // 总页数
            page: 1, // 当前页码
            searchEntity : {}, // 查询条件
            ids : [], // 品牌id数组
            checked : false // 控制全选是否选中
        },
        methods : { // 操作方法
            // 查询品牌方法
            search(page) {
                // 发送异步请求
                axios.get("/brand/findByPage?page=" + page,
                    {params : this.searchEntity}).then(response => {
                    // 获取响应数据 response.data : {pages : 100, rows : [{},{}]}
                    this.dataList = response.data.rows;
                    // 设置总页数
                    this.pages = response.data.pages;
                    // 设置当前页码
                    this.page = page;
                    // 清空数组
                    this.ids = [];
                });
            },
            // 添加或修改
            saveOrUpdate() {
                // 添加
                var url = "save";
                // 判断id
                if (this.entity.id){
                    url = "update"; // 修改
                }
                // 发送异步请求
                axios.post("/brand/" + url, this.entity).then(response => {
                    // 获取响应数据 response.data : true|false
                    if (response.data){
                        // 操作成功,重新查询品牌
                        this.search(this.page);
                    }else{
                        alert("操作失败！");
                    }
                });
            },
            // 显示修改
            show(entity) {
                // 把传过来的entity复制成一个新的json对象
                this.entity = Object.assign({}, entity);
            },
            // 全选事件
            checkAll(e) {
                // 清空数组
                this.ids = [];
                // e.target: dom元素
                // 判断checkbox是否选中
                if(e.target.checked){  // 选中
                    this.dataList.forEach(item => {
                        this.ids.push(item.id);
                    });
                }
            },
            // 删除品牌
            del() {
                if (this.ids.length > 0){
                    if (confirm("您确定要删除?")){
                        axios.get("/brand/delete?ids="
                            + this.ids).then(response => {
                            if (response.data){
                                // 如果当前页码等于总页数，并且全选是选中的
                                let page = (this.page == this.pages
                                    && this.checked) ? this.page - 1 : this.page;
                                if (page < 1){
                                    page = 1;
                                }
                                // 重新查询数据
                                this.search(page);
                            }else {
                                alert("删除失败！");
                            }
                        });
                    }
                }else {
                    alert("请选择要删除的品牌！")
                }
            }
        },
        created() { // 生命周期中方法(自动调用)
            // 查询品牌
            this.search(this.page);
        },
        updated() { // 数据模型中的变量发生改变
            this.checked = (this.ids.length == this.dataList.length);
        }
    });
};