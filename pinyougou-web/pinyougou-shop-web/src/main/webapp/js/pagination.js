var pageComponent = Vue.extend({
    template: '<nav style="margin-top: -30px" aria-label="Page navigation">' +
    '<ul class="pagination">' +
        '<li :class="{\'disabled\':curPage==1}"> ' +
            '<a href="javascript:;" @click="goPage(curPage==1?1:curPage-1)" aria-label="Previous">' +
                '<span aria-hidden="true">&laquo;</span>' +
            '</a>' +
        '</li>' +
        '<li v-for="page in showPageBtn" :class="{\'active\':page==curPage}">' +
            '<a href="javascript:;" v-if="page" @click="goPage(page)">{{page}}</a>' +
            '<a href="javascript:;" v-else>···</a> ' +
        '</li>' +
        '<li :class="{\'disabled\':curPage==pages}">' +
            '<a href="javascript:;" @click="goPage(curPage==pages?pages:curPage+1)" aria-label="Next">' +
                '<span aria-hidden="true">&raquo;</span> ' +
            '</a> ' +
        '</li>' +
        '<li style="line-height: 32px;">&nbsp;&nbsp;共<b>{{pages}}</b>页。</li>' +
    '</ul></nav>',
    props: {
        pages: {type: Number, default: 1},
        current: {type: Number, default: 1}
    },
    data: function () {
        return {curPage: 1}
    },
    computed: {
        showPageBtn: function () {
            this.curPage = this.current;
            if (this.curPage > this.pages){
                this.curPage = this.pages;
            }
            var pageNum = this.pages;
            var index = this.curPage;
            var arr = [];

            if (pageNum > 10){
                if (index <= 7) {
                    return [1,2,3,4,5,6,7,8,0, pageNum];
                } else if (index >= pageNum - 7){
                    return [1, 0, pageNum - 8, pageNum - 7,pageNum - 6,pageNum - 5,
                            pageNum - 4, pageNum - 3, pageNum - 2, pageNum - 1, pageNum];
                }else{
                    return [1, 0, index - 4,index - 3,index - 2,index - 1, index, index + 1,
                            index + 2,index + 3,index + 4, 0, pageNum];
                }
            }else {
                for (var i = 1; i <= pageNum; i++) {
                    arr.push(i);
                }
                return arr;
            }
        }
    }, methods: {
        goPage: function (page) {
            if (page != this.curPage) {
                console.log(page);
                this.curPage = page;
                this.$emit('navpage', this.curPage);
            } else {
                console.log('Already in the current page');
            }
        }
    }
});
Vue.component('pagination', pageComponent);