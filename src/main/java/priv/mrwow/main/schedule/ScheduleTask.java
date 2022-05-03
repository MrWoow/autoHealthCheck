package priv.mrwow.main.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import priv.mrwow.main.Service.IndexService;
import priv.mrwow.main.mapper.UserMapper;
import priv.mrwow.main.model.PO.User;
import priv.mrwow.main.model.example.UserExample;

import java.util.List;

/**
 * @author Mr.Wow
 * @date 5/3/2022 7:56 PM
 */
@Configuration
@EnableScheduling
public class ScheduleTask {
    private final UserMapper userMapper;

    public ScheduleTask(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Scheduled(cron = "0 0 8 * * ?")
    private void configureTasks() {
        List<User> users = userMapper.selectByExample(new UserExample());
        for (User user : users) {

        }
    }
}
