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
            checked : false, // 全选复选框是否选中
            contentCategoryList : [] // 广告类型数组
        },
        methods : { // 定义操作方法
            search : function(page){ // 搜索方法
                // 发送异步请求
                axios.get("/content/findByPage?page=" + page,
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
            saveOrUpdate : function () { // 添加或修改
                var url = "save"; // 添加
                if (this.entity.id){
                    url = "update"; // 修改
                }
                // 发送异步请求
                axios.post("/content/" + url, this.entity)
                    .then(function(response){
                    // 获取响应数据
                    if (response.data){ // 操作成功
                        // 重新加载数据
                        vue.search(vue.page);
                    }else {
                        alert('操作失败！');
                    }
                });
            },
            show : function(entity){ // 数据回显
                // 把entity对象转化成json字符串
                var jsonStr = JSON.stringify(entity);
                // 把json字符串转化成一个新的json对象
                this.entity = JSON.parse(jsonStr);
            },
            checkAll : function (e) { // 全选复选框
                this.ids = []; // 先清空数组
                if (e.target.checked){ // 判断复选框是否选中
                    for (var i = 0; i < this.dataList.length; i++){
                        this.ids.push(this.dataList[i].id);
                    }
                }
            },
            del : function () { // 删除
                if (this.ids.length > 0){
                    axios.get("/content/delete?ids="
                        + this.ids).then(function(response){
                        if (response.data){
                            // 计算当前页码(如果删除为最后一页查询上一页)
                            var page = vue.page == vue.pages && vue.checked
                                ? vue.page - 1 : vue.page;
                            // 重新加载数据
                            vue.search(page);
                        }else{
                            alert("删除失败！");
                        }
                    });
                }else {
                    alert("请选择要删除的记录！");
                }
            },
            // 查询广告类型
            findContentCategory (){
                // 发送异步请求
                axios.get("/contentCategory/findAll").then(response => {
                    // 获取响应数据
                    this.contentCategoryList = response.data;
                });
            },
            // 文件上传
            uploadFile (){
                // 创建表单数据对象
                var formData = new FormData();
                // 追加上传的文件数据
                // 第一个参数：请求参数名称
                // 第二个参数：上传文件的dom对象
                // <input type="file" id="file" />
                formData.append("file", file.files[0]);
                // 发送异步请求
                axios({
                    method : 'post', // 请求方式
                    url : '/upload', // 请求URL
                    data :  formData  // 表单数据(封装上传的文件)
                }).then(response => {
                    // 获取响应数据 response.data : {status : 200, url : ''}
                    if (response.data.status == 200) {
                        this.entity.pic = response.data.url;
                    }else{
                        alert("上传文件失败！");
                    }
                });
            }
        },
        created : function () { // 创建生命周期(初始化方法)
            // 调用搜索方法
            this.search(this.page);
            // 查询广告类型
            this.findContentCategory();
        },
        updated : function () { // 更新数据生命周期
            // 检查全选checkbox是否选中
            this.checked = (this.ids.length == this.dataList.length);
        }
    });
};