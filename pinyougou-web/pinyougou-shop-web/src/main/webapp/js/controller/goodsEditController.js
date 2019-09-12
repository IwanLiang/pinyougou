// 窗口加载完
$(function(){
    // 创建Vue对象
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            goods : {goodsDesc : {itemImages : [],
                                  customAttributeItems : [],
                                  specificationItems : []},
                     category1Id : '',
                     category2Id : '',
                     category3Id : '',
                     typeTemplateId : '',
                     brandId : '',
                     items : [],
                     isEnableSpec : 0}, // 数据封装对象(表单)
            picEntity : {color : '', url : ''}, // 封装一张图片
            itemCatList1 : [], // 一级商品分类
            itemCatList2 : [], // 二级商品分类
            itemCatList3 : [], // 三级商品分类
            brandList : [],    // 品牌数据
            specList : []      // 规格选项
        },
        methods : { // 定义操作方法
            saveOrUpdate : function () { // 添加或修改
                // 获取富文本编辑器中的内容
                this.goods.goodsDesc.introduction = editor.html();

                // 发送异步请求
                axios.post("/goods/save", this.goods)
                    .then(function(response){
                    // 获取响应数据
                    if (response.data){ // 操作成功
                        // 清空表单数据
                        vue.goods = {goodsDesc : {itemImages : [],
                                                customAttributeItems : [],
                                                specificationItems : []},
                                    category1Id : '',
                                    category2Id : '',
                                    category3Id : '',
                                    typeTemplateId : '',
                                    brandId : '',
                                    items : []};
                        // 清空富文本编辑器中的内容
                        editor.html("");
                    }else {
                        alert('操作失败！');
                    }
                });
            },
            // 图片异步上传
            uploadFile : function () {
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
                    data :  formData,  // 表单数据(封装上传的文件)
                    headers : {"Content-Type" : "multipart/form-data"} // 请求头，告诉WEB服务器，请求参数是文件
                }).then(function(response){
                    // 获取响应数据 response.data : {status : 200, url : ''}
                    if (response.data.status == 200) {
                        vue.picEntity.url = response.data.url;
                    }else{
                        alert("上传文件失败！");
                    }
                });
            },
            // 添加商品图片
            addPic : function () {
                this.goods.goodsDesc.itemImages.push(this.picEntity);
            },
            // 删除商品图片
            removePic : function (idx) {
                this.goods.goodsDesc.itemImages.splice(idx, 1);
            },
            // 根据父级id查询商品分类
            findItemCatByParentId : function (parentId, name) {
                // 发送异步请求
                axios.get("/itemCat/findItemCatByParentId?parentId="
                    + parentId).then(function(response){
                    // 获取响应数据 List<ItemCat> [{},{}]
                    vue[name] = response.data;
                });
            },
            // 选择规格选项
            selectSpecOption(e, specName, optionName){
                /**
                 * goods.goodsDesc.specificationItems =
                 * [{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"},
                 *  {"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
                 */
                // 从规格选项数组中搜索一个元素(json对象)
                var obj = this.searchJsonByKey(this.goods.goodsDesc.specificationItems,
                        "attributeName", specName);
                if (obj){
                    // obj : {"attributeValue":["64G","128G"],"attributeName":"机身内存"}
                    if (e.target.checked){ // 选中
                        // 往数组中添加元素
                        obj.attributeValue.push(optionName);
                    }else { // 没有选中

                        // 获取元素在数组中的索引号
                        var idx = obj.attributeValue.indexOf(optionName);
                        // 从数组中删除元素
                        obj.attributeValue.splice(idx,1);

                        // [ { "attributeValue": [], "attributeName": "网络" },
                        // { "attributeValue": [ "256G" ], "attributeName": "机身内存" } ]
                        if(obj.attributeValue.length == 0){
                            // 获取元素在数组中的索引号
                            var idx = this.goods.goodsDesc.specificationItems.indexOf(obj);
                            // 从数组中删除元素
                            this.goods.goodsDesc.specificationItems.splice(idx,1);
                        }
                    }
                }else {
                    this.goods.goodsDesc.specificationItems
                        .push({attributeValue: [optionName], attributeName: specName});
                }
            },
            // 从规格选项数组中搜索一个元素(json对象)
            searchJsonByKey(jsonArr, key, value){
                /**
                 * jsonArr
                 * [{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"},
                 *  {"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
                 */
                for (var item of jsonArr){
                    // item : {"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"}
                    if (item[key] == value){
                        return item;
                    }
                }
                return null;
            },
            // 生成SKU数组
            createItems() {
                // 初始化items数组
                // spec: {"网络":"联通4G","机身内存":"64G"}
                this.goods.items = [{spec:{}, price:0, num:9999,status:'0', isDefault:'0'}];

                // [ { "attributeValue": [ "移动4G", "联通4G", "电信4G" ], "attributeName": "网络" } ]
                // 获取用户选中的规格选项
                var specItems = this.goods.goodsDesc.specificationItems;
                // 迭代规格选项数组
                // 参数名 => 函数体
                specItems.forEach(specItem => {
                    // specItem: { "attributeValue": [ "移动4G", "联通4G", "电信4G" ], "attributeName": "网络" }
                    // 扩充goods.items数组，返回修改后的数组
                    this.goods.items = this.swapItems(this.goods.items,
                        specItem.attributeValue, specItem.attributeName);
                });
            },
            // 扩充SKU数组的方法
            swapItems(items, attributeValue, attributeName){
                // items : [{spec:{}, price:0, num:9999,status:'0', isDefault:'0'}]
                // attributeValue :  [ "移动4G", "联通4G", "电信4G" ]
                // attributeName : 网络
                // 定义新的SKU数组
                var newItems = [];
                // 迭代旧的items
                for (var item of items){ // 1
                    // item: {spec:{}, price:0, num:9999,status:'0', isDefault:'0'}
                    // attributeValue :  [ "移动4G", "联通4G", "电信4G" ]
                    for (var optionName of attributeValue){ // 3
                        // optionName : 移动4G
                        // spec: {"网络":"联通4G","机身内存":"64G"}
                        // 克隆产生新的item
                        var newItem = JSON.parse(JSON.stringify(item));
                        // 设置spec
                        newItem.spec[attributeName] = optionName;
                        // 添加元素
                        newItems.push(newItem);
                    }
                }
                return newItems;
            }
        },
        watch : { // 监控data中的变量
            // 监控goods.category1Id一级分类id发生改变，查询二级分类
            "goods.category1Id" : function (newVal, oldVal) {
                // alert("新值：" + newVal + ", 旧值：" + oldVal);
                // 清空二级分类id
                this.goods.category2Id = "";
                if (newVal){ // 不是空 ""、null、undefined
                    // 发送异步请求查询二级分类
                    this.findItemCatByParentId(newVal, "itemCatList2");
                }else{
                    // 清空二级分类数组
                    this.itemCatList2 = [];
                }
            },
            // 监控goods.category2Id二级分类id发生改变，查询三级分类
            "goods.category2Id" : function (newVal, oldVal) {
                // 清空三级分类id
                this.goods.category3Id = "";
                if (newVal){ // 不是空 ""、null、undefined
                    // 发送异步请求查询三级分类
                    this.findItemCatByParentId(newVal, "itemCatList3");
                }else{
                    // 清空二级分类数组
                    this.itemCatList3 = [];
                }
            },
            // 监控goods.category3Id三级分类id发生改变，获取类型模板id
            "goods.category3Id" : function (newVal, oldVal) {
                // 清空类型模板id
                this.goods.typeTemplateId = '';
                if (newVal){ // 不是空 ""、null、undefined
                    // 从三级分类数组中查询对应的类型模板
                    for (var i = 0; i < this.itemCatList3.length; i++){
                        var itemCat = this.itemCatList3[i];
                        if (itemCat.id == newVal){
                            this.goods.typeTemplateId = itemCat.typeId;
                            break;
                        }
                    }
                }
            },
            // 监控goods.typeTemplateId类型模板id发生改变，查询类型模板对象
            "goods.typeTemplateId"(newVal, oldVal) {
                // 清空品牌id
                this.goods.brandId = '';
                if (newVal){ // 不是空 ""、null、undefined
                    // 发送异步请求根据主键id查询类型模板对象
                    axios.get("/typeTemplate/findOne?id=" + newVal).then(response => {
                        // 获取响应数据 TypeTemplate : {}
                        // 品牌数据(转化成数组)
                        this.brandList = JSON.parse(response.data.brandIds);
                        // 扩展属性(转化成数组)
                        this.goods.goodsDesc.customAttributeItems =
                            JSON.parse(response.data.customAttributeItems);

                    });
                    // 发送异步请求根据主键id查询规格选项数据
                    axios.get("/typeTemplate/findSpecOptionsByTemplateId?id="
                        + newVal).then(response => {
                         // 获取响应数据
                        /**
                         * [{"id":27,"text":"网络","options" : [{optionName : ''},{}]},
                         * {"id":32,"text":"机身内存","options" : [{optionName : ''},{}]}]
                         */
                        this.specList = response.data;
                    });
                }else{
                    // 清空品牌数组
                    this.brandList = [];
                    // 清空扩展属性数组
                    this.goods.goodsDesc.customAttributeItems = [];
                    // 清空规格选项数组
                    this.specList = [];
                }
            }
        },
        created : function () { // 创建生命周期(初始化方法)
            this.findItemCatByParentId(0, "itemCatList1");
        }
    });
});