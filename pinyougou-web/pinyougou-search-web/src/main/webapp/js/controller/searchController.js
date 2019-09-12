// 窗口加载完
window.onload = function () {

    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            searchParam : {keywords : '', category : '', brand : '', price : '',
                           spec : {}, page : 1, sortField: '', sortValue : ''}, // 搜索条件
            resultMap : {}, // 搜索返回的数据
            pageNums : [], // 页码数组
            keywords : '',  // 搜索关键字
            jumpPage : 1,   // 跳转的页码
            firstDot : false, // 前面不加点
            lastDot : false // 后面不加点
        },
        methods : { // 操作方法
            // 商品搜索方法
            search (){
                // 发送异步请求
                axios.post("/search", this.searchParam).then(response => {
                    // 获取响应数据 response.data : {total : 1000, rows : [{},{}]}
                    this.resultMap = response.data;
                    // 设置用户搜索条件
                    this.keywords = this.searchParam.keywords;
                    // 初始化页码数组
                    this.initPageNum();
                });
            },
            // 初始化页码数组
            initPageNum (){
                // 清空页码数组
                this.pageNums = [];

                this.firstDot = false; // 前面不加点
                this.lastDot =  false; // 后面不加点

                // 开始页码
                var firstPage = 1;
                // 结束页码
                var lastPage = this.resultMap.totalPages;

                // 判断总页数
                if (this.resultMap.totalPages > 5){
                    // 判断当前页码是否离首页近些
                    if (this.searchParam.page <= 3){
                        lastPage = 5;
                        this.lastDot =  true; // 后面加点
                    }else if (this.searchParam.page >= this.resultMap.totalPages - 3){
                        // 判断当前页码是否离尾页近些
                        firstPage = this.resultMap.totalPages - 4;
                        this.firstDot = true; // 前面加点
                    }else{
                        // 在中间
                        firstPage = this.searchParam.page - 2;
                        lastPage = this.searchParam.page  + 2;
                        this.firstDot = true; // 前面加点
                        this.lastDot =  true; // 后面加点
                    }
                }

                // 循环产生页码
                for (var i = firstPage; i <= lastPage; i++){
                    this.pageNums.push(i);
                }
            },
            // 添加过滤条件
            addSearchItem (key, value){
                // 判断：商品分类、商品品牌、价格区间
                if (key == 'category' || key == 'price' || key == 'brand') {
                    this.searchParam[key] = value;
                }else{
                    // 规格选项
                    this.searchParam.spec[key] = value;
                }
                // 执行搜索
                this.search();
            },
            // 删除过滤条件
            removeSearchItem (key){
                // 判断：商品分类、商品品牌、价格区间
                if (key == 'category' || key == 'price' || key == 'brand') {
                    this.searchParam[key] = '';
                }else{
                    // 规格选项
                    delete this.searchParam.spec[key];
                }
                // 执行搜索
                this.search();
            },
            // 分页搜索
            pageSearch (page){
                // v-model 绑定的数据都是字符串
                page = parseInt(page);
                // 判断页码的有效性
                if (page >= 1 && page <= this.resultMap.totalPages
                    && page != this.searchParam.page){
                    this.searchParam.page = page;
                    this.jumpPage = page;
                    // 执行搜索
                    this.search();
                }
            },
            // 排序搜索
            sortSearch (sortField, sortValue){
                this.searchParam.sortField = sortField;
                this.searchParam.sortValue = sortValue;
                // 执行搜索
                this.search();
            },
            // 初始化搜索
            initSearch (){
                // 获取请求URL后面的参数
                this.searchParam.keywords = this.getUrlParam("keywords");
                //  商品搜索方法
                this.search();
            }
        },
        created : function () { // 创建生命周期
            this.initSearch();
        }
    });
};