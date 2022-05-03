package priv.mrwow.main.Service;

import priv.mrwow.main.model.PO.Apply;

import java.util.List;

/**
 * @author Mr.Wow
 * @date 5/3/2022 7:58 PM
 */
public interface IndexService {
    List<Apply> getAllApplyList();

    String applyInfo(String username, String password, String name, String comment);

    String admitOneUser(int id);

    String rejectOneUser(int id);
}
