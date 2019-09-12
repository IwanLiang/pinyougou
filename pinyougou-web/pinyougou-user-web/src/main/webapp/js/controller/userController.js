// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            user : {}, // 用户数据
            password : '', // 确认密码
            disabled : false, // 是否禁用
            tip : '获取短信验证码', // 提示文本
            code : '' // 短信验证码
        },
        methods : { // 操作方法
            // 用户注册
            save (){
                // 判断是否为空
                if (!this.user.password){
                    alert("密码不能为空！");
                }else {
                    // 判断两次密码是否一致
                    if (this.password && this.password == this.user.password) {
                        // 发送异步请求
                        axios.post("/user/save?code=" + this.code, this.user).then(response => {
                            // 获取响应数据
                            if (response.data) {
                                // 清空数据
                                this.user = {};
                                this.password = "";
                                this.code = "";
                            } else {
                                alert("用户注册失败！");
                            }
                        });
                    } else {
                        alert("两次密码不一致！");
                    }
                }
            },
            // 发送短信验证码
            sendSmsCode (){
                // 判断手机号码
                if (this.user.phone && /^1[3|4|5|6|7|8|9]\d{9}$/.test(this.user.phone)){
                    // 发送异步请求
                    axios.get("/user/sendSmsCode?phone=" + this.user.phone).then(response => {
                        // 获取响应数据
                        if (response.data){
                            // 发送成功，开启倒计时
                            this.downCount(90);
                        }else{
                            alert("短信验证码发送失败！");
                        }
                    });
                }else{
                    alert("手机号码格式不正确！");
                }
            },
            // 倒计时方法
            downCount (seconds){
                this.disabled = true; // 是否禁用
                seconds--;
                if (seconds >= 0) {
                    this.tip = seconds + 'S，后重新获取'; // 提示文本
                    // 开启定时器
                    window.setTimeout(() => {
                        this.downCount(seconds);
                    }, 1000);
                }else {
                    this.disabled = false; // 是否禁用
                    this.tip = "获取短信验证码";
                }
            }
        }
    });
};