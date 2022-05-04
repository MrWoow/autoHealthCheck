package priv.mrwow.main.ServiceImpl;

import org.springframework.stereotype.Service;
import priv.mrwow.main.Service.IndexService;
import priv.mrwow.main.mapper.ApplyMapper;
import priv.mrwow.main.mapper.UserMapper;
import priv.mrwow.main.model.PO.Apply;
import priv.mrwow.main.model.PO.User;
import priv.mrwow.main.model.example.ApplyExample;

import java.util.List;

/**
 * @author Mr.Wow
 * @date 5/3/2022 7:59 PM
 */
@Service
public class IndexServiceImpl implements IndexService {
    private final UserMapper userMapper;

    private final ApplyMapper applyMapper;

    public IndexServiceImpl(UserMapper userMapper, ApplyMapper applyMapper) {
        this.userMapper = userMapper;
        this.applyMapper = applyMapper;
    }


    @Override
    public List<Apply> getAllApplyList() {
        ApplyExample example = new ApplyExample();
        return applyMapper.selectByExample(example);
    }

    @Override
    public User applyInfo(String username, String password, String name, String comment) {
        Apply apply = new Apply();
        apply.setUsername(username);
        apply.setPassword(password);
        apply.setName(name);
        apply.setComment(comment);
        applyMapper.insertSelective(apply);
        return new User();
    }

    @Override
    public User admitOneUser(int id) {
        Apply apply = applyMapper.selectByPrimaryKey(id);
        User user = new User();
        user.setUsername(apply.getUsername());
        user.setPassword(apply.getPassword());
        user.setName(apply.getName());
        userMapper.insertSelective(user);
        applyMapper.deleteByPrimaryKey(id);
        return new User();
    }

    @Override
    public User rejectOneUser(int id) {
        applyMapper.deleteByPrimaryKey(id);
        return new User();
    }
}
