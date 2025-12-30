package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")

public class UserContoller {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;
    @Autowired
    TransactionRepo transactionRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);
        return "Signup Sucessfull";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u)
    {
        User user = userRepo.findByUsername(u.getUsername());
        if(user==null)
        {
            return "user not found";
        }
        if(!u.getPassword().equals(user.getPassword()))
        {
            return "password incorrect";
        }
        if(!u.getRole().equals(user.getRole()))
        {
            return "incorrect role";
        }
        return String.valueOf(user.getId());
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }
    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        User user =userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException());

        if(obj.getKey().equalsIgnoreCase("name"))
        {
            if (user.getName().equalsIgnoreCase(obj.getValue())) return "cannot be same";
            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password"))
        {
            if (user.getPassword().equalsIgnoreCase(obj.getValue())) return "cannot be same";
            user.setPassword(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("email"))
        {
            if (user.getEmail().equalsIgnoreCase(obj.getValue())) return "cannot be same";
            User user2 =userRepo.findByEmail(obj.getValue());
            if(user2!=null) return"email already exist";

            user.setEmail(obj.getValue());
        }
        else
        {
            return "Invalid Key";
        }
        userRepo.save(user);
        return "upadte done succefully";

    }
    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId)
    {
        History h1 = new History();
        h1.setDescription("Admin "+adminId+" Created user "+user.getUsername());
        userRepo.save(user);
        if(user.getBalance()>0) {
            User user2 =userRepo.findByUsername(user.getUsername());
            Transaction t = new Transaction();
            t.setAmount(user.getBalance());
            t.setCurrBalance(user.getBalance());
            t.setDescription("Rs "+user.getBalance()+"Deposit Successful");
            t.setUserId(user2.getId());
            transactionRepo.save(t);

        }

        historyRepo.save(h1);
        return "sucessfully added";
    }
    @DeleteMapping("delete-user/{userId}/admin/{adminId}")
    public String delete(@PathVariable int userId,@PathVariable int adminId)
    {
        User user = userRepo.findById(userId).orElseThrow(()->new RuntimeException());
        if(user.getBalance()>0)
        {
            return "balance Should be zero";
        }
        History h1 =new History();
        h1.setDescription("Admin "+adminId+" Delete User "+user.getUsername());
        historyRepo.save(h1);
        userRepo.delete(user);
        return "user deleted Successfully";
    }
    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy,@RequestParam String order)
    {
        Sort sort;
        if(order.equalsIgnoreCase("dese"))
        {
            sort =Sort.by(sortBy).descending();
        }
        else
        {
            sort =Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer",sort);
    }
    @GetMapping("/users/{keyword}")
    public List<User> getUser(@PathVariable String keyword)
    {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }
}