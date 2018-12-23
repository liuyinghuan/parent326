//控制层
app.controller('seckillGoodsController' ,function($scope,$interval,$location,seckillOrderService,seckillGoodsService){
//读取列表数据绑定到表单中
    $scope.findList=function(){
       // alert("11");
        seckillGoodsService.findList().success(
            function(response){
                //alert(response);
                $scope.goodsList=response;
            }
        );
    }


    $scope.findOne=function () {
        seckillGoodsService.findOne($location.search()['id']).success(function (response) {

            $scope.entity=response;

            //得到当前时间到截止时间的总秒数
            allsecond=Math.floor((((new Date(response.endTime)).getTime())-(new Date()).getTime())/1000);
            //获取秒杀距离当前时间得字符串
            time=$interval(function () {
                allsecond--;
                $scope.timeString=convertTimeString(allsecond);
                //alert($scope.timeString);
                if(allsecond==0){
                    $interval.cancel(time);
                }
            },1000);


        })

    }

    // 转换秒数为时间字符串 XX天 XX:XX:XX
    convertTimeString=function (allsecond) {
        //天数
        var days=  Math.floor(allsecond/(60*60*24))
        //小时数
        var hours= Math.floor((allsecond-days*60*60*24)/(60*60))
        //分钟数
        var minutes=  Math.floor((allsecond-days*60*60*24- hours*60*60)/60)
        //秒数
        var second= allsecond-days*60*60*24- hours*60*60 -minutes*60
        var timeString="";
        if(days>0){
            timeString+= days+"天"
        }
        timeString+= hours+":"+minutes+":"+second;
        return timeString;
    }

    //提交秒杀订单
    //提交订单
    $scope.submitOrder=function () {
        //alert("1")
        seckillOrderService.submitOrder($scope.entity.id).success(
            function (response) {
                if(response.flag){
                    //成功
                    alert("下单成功！");
                    location.href="pay.html";
                }else{
                    alert(response.message);
                }
            }
        )
    }
});