// 窗口加载完
$(function(){

    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            num : 1, // 购买数量
            spec : {}, // 用户选择的规格选项
            sku : {} // SKU对象
        },
        methods : { // 操作方法
            // 购买数量加减操作
            addNum (x){
                this.num = parseInt(this.num);
                this.num += x;
                if (this.num < 1){
                    this.num = 1;
                }
            },
            // 记录用户选择的规格选项
            selectSpec (key, value){
                // 为json 对象设置key与value
                Vue.set(this.spec,key,value);
                // 根据规格搜索对应的SKU
                this.searchSku();
            },
            // 判断是否为选中的规格
            isSelectedSpec (key, value){
                return this.spec[key] == value;
            },
            // 加载默认的SKU
            loadSku (){
                // 从SKU数组中取一个元素
                this.sku = itemList[0];
                // 获取该SKU的spec
                this.spec = JSON.parse(this.sku.spec);
            },
            // 根据规格搜索对应的SKU
            searchSku (){
                for(item of itemList){
                    if (item.spec == JSON.stringify(this.spec)){
                        this.sku = item;
                        break;
                    }
                }
            },
            // 加入购物车按钮
            addToCart (){
               // alert("sku商品的id:" + this.sku.id + ",购买数量：" + this.num);
                // 发送跨域的异步请求 http://item.pinyougou.com ==> http://cart.pinyougou.com
                axios.get("http://cart.pinyougou.com/cart/addCart?itemId="
                    + this.sku.id + "&num=" + this.num, {withCredentials : true}).then(response => {
                        if(response.data){
                            // 跳转到购物车系统
                            location.href = "http://cart.pinyougou.com";
                        }else {
                            alert("加入购物车失败！");
                        }
                });

            }
        },
        created : function () { // 创建生命周期
            this.loadSku();
        }
    });
});