package com.timmy.mapper;

import com.timmy.entity.EnrollInfo;
import com.timmy.entity.UserInfo;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface EnrollInfoMapper {
 

    int deleteByPrimaryKey(Integer id);

    int deleteByEnrollId(Long id);

    int insertSelective(EnrollInfo record);

    int insertEnrollInfo(EnrollInfo record);

    EnrollInfo selectByPrimaryKey(Integer id);
    
    int updateByPrimaryKeySelective(EnrollInfo record);

    int updateByPrimaryKeyWithBLOBs(EnrollInfo record);

    
    int insert(@Param("enrollId")Long enrollid,@Param("backupnum")int backupnum,@Param("imagePath")String imagePath,@Param("signatures")String signature);
   
    EnrollInfo selectByBackupnum(@Param("enrollId")Long enrollId,@Param("backupnum")int backupnum);
       
    List<EnrollInfo> selectAll();
    
    List<EnrollInfo> selectByEnrollId(Long enrollId);
    
    int updateByEnrollIdAndBackupNum(@Param("signatures")String signatures,@Param("enrollId")Long enrollId,@Param("backupnum")int backupnum);
}