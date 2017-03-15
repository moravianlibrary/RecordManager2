package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.api.model.user.ApprovedUsetDto;
import cz.mzk.recordmanager.api.model.user.LoginUserDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/login")
public class AuthController {

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ApprovedUsetDto login(@RequestBody LoginUserDto user){

		ApprovedUsetDto approvedUsetDto = new ApprovedUsetDto();

		if (user.getLogin().equals("root") && user.getPassword().equals("testPassword123")){
			approvedUsetDto.setLogin("root");
			approvedUsetDto.setToken("token");
		}
		return approvedUsetDto;
	}

}

