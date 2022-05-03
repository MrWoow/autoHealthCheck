package priv.mrwow.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import priv.mrwow.main.Service.IndexService;
import priv.mrwow.main.model.PO.Apply;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.Wow
 * @date 5/3/2022 7:15 PM
 */
@Controller
@RequestMapping(value = "/auto")
public class IndexController {
    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @RequestMapping(value = "/apply")
    public String apply() {
        return "apply";
    }

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    @ResponseBody
    public String applyInfo(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String comment = request.getParameter("comment");
        return indexService.applyInfo(username, password, name, comment);
    }

    @RequestMapping(value = "/authorize")
    public String authorize() {
        return "authorize";
    }

    @RequestMapping(value = "/getAllApply")
    @ResponseBody
    public List<Apply> getApplyList() {
        return indexService.getAllApplyList();
    }

    @RequestMapping(value = "/admit")
    @ResponseBody
    public String admit(int id) {
        return indexService.admitOneUser(id);
    }

    @RequestMapping(value = "/reject")
    @ResponseBody
    public String reject(int id) {
        return indexService.rejectOneUser(id);
    }
}
