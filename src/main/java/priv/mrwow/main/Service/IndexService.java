package priv.mrwow.main.Service;

import priv.mrwow.main.model.PO.Apply;
import priv.mrwow.main.model.PO.User;

import java.util.List;

/**
 * @author Mr.Wow
 * @date 5/3/2022 7:58 PM
 */
public interface IndexService {
    List<Apply> getAllApplyList();

    User applyInfo(String username, String password, String name, String comment);

    User admitOneUser(int id);

    User rejectOneUser(int id);
}
