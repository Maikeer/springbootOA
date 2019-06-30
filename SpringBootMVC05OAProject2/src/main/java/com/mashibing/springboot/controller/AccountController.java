package com.mashibing.springboot.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;
import com.mashibing.springboot.RespStat;
import com.mashibing.springboot.entity.Account;
import com.mashibing.springboot.service.AccountService;


/**
 * 用户账户相关
 * @author Administrator
 *
 */

@Controller
@RequestMapping("/account")
public class AccountController {

	
	@Autowired
	AccountService accountSrv;
	
	
	
	@RequestMapping("login")
	public String login() {
		
		return "account/login";
	}
	

	/**
	 * 用户登录异步校验
	 * @param loginName
	 * @param password
	 * @return success 成功
	 */
	@RequestMapping("validataAccount")
	@ResponseBody
	public String validataAccount(String loginName,String password,HttpServletRequest request) {
		
		System.out.println("loginName:" + loginName);
		System.out.println("password:" + password);
		
		// 1. 直接返回是否登录成功的结果
		// 2. 返回 Account对象，对象是空的 ，在controller里做业务逻辑
		// 在公司里 统一写法
		
	
		//让service返回对象，如果登录成功 把用户的对象 
		Account account = accountSrv.findByLoginNameAndPassword(loginName, password);
		
		if (account == null) {
			return "登录失败";
		}else {
			// 登录成功
			// 写到Session里
			// 在不同的controller 或者前端页面上 都能使用 
			// 当前登录用户的Account对象
			
			request.getSession().setAttribute("account", account);
			return "success";
		}
	}
	
	
	@RequestMapping("/logOut")
	public String logOut(HttpServletRequest request) {
		
		request.getSession().removeAttribute("account");
		return "index";
	}
	@RequestMapping("/list")
	public String list(@RequestParam(defaultValue = "1") int pageNum,@RequestParam(defaultValue = "5" ) int pageSize,Model model) {
		
		PageInfo<Account>page = accountSrv.findByPage(pageNum,pageSize);
		
		model.addAttribute("page", page);
		return "/account/list";
	}
	
	@RequestMapping("/deleteById")
	@ResponseBody
	public RespStat deleteById(int id) {
		// 标记一下 是否删除成功？  status
		RespStat stat = accountSrv.deleteById(id);
		
		return stat;
	}
	
	
	// FastDFS
	
	
	
	@RequestMapping("/profile")
	public String profile () {
		
		try {
			  File path = new File(ResourceUtils.getURL("classpath:").getPath());
//		        File upload = new File(path.getAbsolutePath(), "static/upload/");
		        File upload = new File("G:/upload/");
		        System.out.println(upload.getAbsolutePath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		return "account/profile";
	}
	
	
	/**
	 * 中文字符
	 * @param filename
	 * @param password
	 * @return
	 */
	@RequestMapping("/fileUploadController")
	public String fileUpload (MultipartFile filename,String password,Integer id) {
		System.out.println("id:" + id);
		System.out.println("password:" + password);
		System.out.println("file:" + filename.getOriginalFilename());
		try {
			
		File path = new File(ResourceUtils.getURL("classpath:").getPath());
//        File upload = new File(path.getAbsolutePath(), "static/upload/");
		 File upload = new File("G:/upload/");
        
        System.out.println("upload:" + upload);
      //根据id获取客户信息，判断原始密码是否正确
      		Account account = accountSrv.findByID(id);
      		String ysPass = account.getPassword();
    		//判断旧密码相同进行新密码修改
    		if(ysPass!=null&&ysPass.equals(password)) {
    			
    			System.err.println("filename.getName():"+filename.getName()+"==filename.getOriginalFilename()"+filename.getOriginalFilename());
    			 filename.transferTo(new File(upload+"/"+filename.getOriginalFilename()));
    			 String imageUri="/"+filename.getOriginalFilename();
    			 account.setImageUri(imageUri);
    			 int passwordById = accountSrv.updatePasswordById(account);
    		}
       
        
       
        
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "account/list";
	}
	@RequestMapping("/register")
	public String register() {
		return "account/register";
		
	}
	/**
	 * 跳转页面
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping("/updatepassword")
	public String updatepassword(int id,Model model) {
		System.err.println("id:"+id);
		model.addAttribute("accountid", id);
		return "account/updatepassword";
		
	}
	/**
	 * 修改密码
	 * @param oldpss
	 * @param newpss
	 * @param id
	 * @return
	 */
	@RequestMapping("/updatepass")
	@ResponseBody
	public RespStat updatepass(String oldpss,String newpss,Integer id) {
		System.err.println("oldpss"+oldpss+"newpss"+newpss+"id"+id);
		//根据id获取客户信息，判断原始密码是否正确
		Account account = accountSrv.findByID(id);
		String password = account.getPassword();
		//判断旧密码相同进行新密码修改
		if(password!=null&&password.equals(oldpss)) {
			account.setPassword(newpss);
			int status=accountSrv.updatePasswordById(account);
			if(status==1) {
				return RespStat.build(200, "注册用户名成功");
			}else {
				return RespStat.build(500, "密码修改失败");
			}
		}
		 return RespStat.build(500, "原密码不正确");
		
	}
	@RequestMapping("/registerUser")
	@ResponseBody
	public RespStat registerUser(Account account) {
		System.err.println(account);
		Account acc=accountSrv.findByName(account.getLoginName());
		System.err.println("Account:"+acc);
		if(acc!=null) {
			return RespStat.build(500, "用户名已存在");
		}
		int n=accountSrv.insertAccount(account);
		if(n==0) {
			RespStat.build(500, "注册用户失败");
		}
		System.err.println("插入用户:"+n);
		return RespStat.build(200, "注册用户名成功");
		
	}
}
