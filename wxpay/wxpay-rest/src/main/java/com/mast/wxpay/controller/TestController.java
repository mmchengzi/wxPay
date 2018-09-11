package com.mast.wxpay.controller;


import com.wordnik.swagger.annotations.Api;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log
@RequestMapping("/infokk")
@Api(value="用户controller",description="用户操作")
public class TestController {
/*	@Autowired
	private InfoKkServie servie;

	@ApiOperation("获取用户信息")
	@GetMapping("/test")
	public String test() {
		User user = new User();
		return user.toString();
	}
	@ApiOperation(value = "根据id查询学生的信息",notes = "查询数据库中某个学生的信息")
	@GetMapping(value = "test/{id}")
	public InfoKk getInfoKk(@ApiParam(value = "id") @PathVariable("id") String id) {
		return servie.selectByPrimaryKey(id);
	}*/

}
