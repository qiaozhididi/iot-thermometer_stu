package iot.cloud.platform.thermometer.mapper;

import iot.cloud.platform.thermometer.vo.TempEmojiVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TempEmojiMapper {

  String getFaceByTemp(int temp);
}
