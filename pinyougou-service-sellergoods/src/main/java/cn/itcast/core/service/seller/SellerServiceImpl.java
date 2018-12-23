package cn.itcast.core.service.seller;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class SellerServiceImpl implements SellerService {

    @Resource
    private SellerDao sellerDao;

    /**
     * 商家入驻申请
     * @param seller
     */
    @Override
    public void add(Seller seller) {
        // 设置商家的审核状态
        seller.setStatus("0"); // 待审核的状态
        seller.setCreateTime(new Date()); // 提交的审核日期
        // 对商家密码加密：MD5、BCrypt、spring盐值
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String password = bCryptPasswordEncoder.encode(seller.getPassword());
        seller.setPassword(password);
        sellerDao.insertSelective(seller);
    }

    /**
     * 待审核商家的列表查询
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page, rows);
        SellerQuery sellerQuery = new SellerQuery();
        if(seller.getStatus() != null && !"".equals(seller.getStatus().trim())){
            sellerQuery.createCriteria().andStatusEqualTo(seller.getStatus().trim());
        }
        Page<Seller> p = (Page<Seller>) sellerDao.selectByExample(sellerQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 查询商家实体对象
     * @param sellerId
     * @return
     */
    @Override
    public Seller findOne(String sellerId) {
        return sellerDao.selectByPrimaryKey(sellerId);
    }

    /**
     * 审核商家
     * @param sellerId
     * @param status
     */
    @Transactional
    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
