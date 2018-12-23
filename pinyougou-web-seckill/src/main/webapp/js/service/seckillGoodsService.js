//服务层
app.service('seckillGoodsService',function($http){
//读取列表数据绑定到表单中
    this.findList=function(){
        //alert("22");
        return $http.get('seckillGoods/findList.do');
    }


    this.findOne=function(id){
        //alert("22");
        return $http.get('seckillGoods/findOne.do?id='+id);
    }
});