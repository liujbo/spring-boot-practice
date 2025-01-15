package com.tsgs.demotkmybatiseasyexcel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.tsgs.demotkmybatiseasyexcel.entity.TbUserInfo;
import com.tsgs.demotkmybatiseasyexcel.mapper.UserInfoMapper;
import com.tsgs.demotkmybatiseasyexcel.util.UserIdGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserInfoListener extends AnalysisEventListener<TbUserInfo> {

    private final List<TbUserInfo> userInfoList = new ArrayList<>();

    @Getter
    @Setter
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void invoke(TbUserInfo data, AnalysisContext context) {
        data.setUserNo(UserIdGenerator.generateUserId());
        userInfoList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("全部解析完成，总条数：{}", userInfoList.size());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
            for (int i = 0; i < userInfoList.size(); i++) {
                userInfoMapper.insert(userInfoList.get(i));
                if (i % 10000 == 0 || i == userInfoList.size() - 1) {
                    sqlSession.commit();
                }
            }
        }
        log.info("=======全部插入数据库完成");
    }

    public UserInfoListener(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
}
