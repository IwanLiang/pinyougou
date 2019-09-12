// 窗口加载完
window.onload = function () {
    // 创建Vue对象
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            dataList : [], // 定义数组，接收后台响应数据
            entity : {typeId : 0}, // 数据封装对象(表单)
            ids : [], // 复选框选中的id数组
            checked : false, // 全选复选框是否选中
            itemCat1 : {}, // 记录一级分类
            itemCat2 : {}, // 记录二级分类
            grade : 1, // 级别的变量
            parentId : 0, // 记录父级id
            typeTemplateList : [] // 类型模板数组
        },
        methods : { // 定义操作方法
            // 根据父级id查询商品分类
            findItemCatByParentId : function(parentId){
                // 发送异步请求
                axios.get("/itemCat/findItemCatByParentId?parentId=" + parentId)
                    .then(function(response){
                        // 获取响应数据
                        vue.dataList = response.data;
                        // 记录父级id
                        vue.parentId = parentId;
                        // 设置ids数组为空
                        vue.ids = [];
                    });
            },
            // 查询下级按钮
            selectList : function (entity, grade) {
                // 设置值
                this.grade = grade;
                if (this.grade == 1){ // 查询一级分类
                    this.itemCat1 = {};
                    this.itemCat2 = {};
                }else if (this.grade == 2){ // 查询二级分类
                    // 记录一级分类(父级)
                    this.itemCat1 = entity;
                    this.itemCat2 = {};
                }else if (this.grade == 3){ // 查询三级分类
                    // 记录二级分类(父级)
                    this.itemCat2 = entity;
                }
                // 根据父级id查询商品分类
                this.findItemCatByParentId(entity.id);
            },
            saveOrUpdate : function () { // 添加或修改
                var url = "save"; // 添加
                if (this.entity.id){
                    url = "update"; // 修改
                }else{
                    // 添加时，传父级id到后台
                    this.entity.parentId = this.parentId;
                }
                // 发送异步请求
                axios.post("/itemCat/" + url, this.entity)
                    .then(function(response){
                    // 获取响应数据
                    if (response.data){ // 操作成功
                        // 重新加载数据
                        vue.findItemCatByParentId(vue.parentId);
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
                    axios.get("/itemCat/delete?ids="
                        + this.ids).then(function(response){
                        if (response.data){
                            // 重新加载数据
                            vue.findItemCatByParentId(vue.parentId);
                        }else{
                            alert("删除失败！");
                        }
                    });
                }else {
                    alert("请选择要删除的记录！");
                }
            },
            // 查询全部的类型模板
            findTypeTemplateList : function () {
               axios.get("/typeTemplate/findTypeTemplateList")
                   .then(function(response){
                   // 获取响应数据 [{id : 1, name : ''},{id : 2, name : ''}]
                   vue.typeTemplateList = response.data;
               });
            }
        },
        created : function () { // 创建生命周期(初始化方法)
            // 根据父级id查询商品分类
            this.findItemCatByParentId(0);
            // 查询全部的类型模板
            this.findTypeTemplateList();
        },
        updated : function () { // 更新数据生命周期
            // 检查全选checkbox是否选中
            this.checked = (this.ids.length == this.dataList.length);
        }
    });
};