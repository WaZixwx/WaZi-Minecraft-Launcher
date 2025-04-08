package com.wazixwx.launcher.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wazixwx.launcher.model.Account;
import com.wazixwx.launcher.utils.LogUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 账号管理服务类
 * Account Management Service Class
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class AccountService {
    private static final String ACCOUNTS_FILE = "accounts.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final TypeToken<List<Account>> ACCOUNT_LIST_TYPE = new TypeToken<List<Account>>() {};
    
    private List<Account> accounts;
    private Account selectedAccount;
    
    /**
     * 构造函数 Constructor
     */
    public AccountService() {
        this.accounts = new ArrayList<>();
        loadAccounts();
    }
    
    /**
     * 加载账号列表
     * Load account list
     */
    private void loadAccounts() {
        try {
            File file = new File(ACCOUNTS_FILE);
            if (file.exists()) {
                String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                accounts = GSON.fromJson(json, ACCOUNT_LIST_TYPE.getType());
                
                // 设置选中的账号 Set selected account
                for (Account account : accounts) {
                    if (account.isSelected()) {
                        selectedAccount = account;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            LogUtils.error("加载账号列表失败 Failed to load accounts", e);
        }
    }
    
    /**
     * 保存账号列表
     * Save account list
     */
    private void saveAccounts() {
        try {
            String json = GSON.toJson(accounts);
            FileUtils.writeStringToFile(new File(ACCOUNTS_FILE), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LogUtils.error("保存账号列表失败 Failed to save accounts", e);
        }
    }
    
    /**
     * 添加账号
     * Add account
     * 
     * @param account 账号 Account
     */
    public void addAccount(Account account) {
        accounts.add(account);
        saveAccounts();
    }
    
    /**
     * 删除账号
     * Delete account
     * 
     * @param account 账号 Account
     */
    public void removeAccount(Account account) {
        accounts.remove(account);
        if (selectedAccount == account) {
            selectedAccount = null;
        }
        saveAccounts();
    }
    
    /**
     * 选择账号
     * Select account
     * 
     * @param account 账号 Account
     */
    public void selectAccount(Account account) {
        if (selectedAccount != null) {
            selectedAccount.setSelected(false);
        }
        account.setSelected(true);
        selectedAccount = account;
        saveAccounts();
    }
    
    /**
     * 获取所有账号
     * Get all accounts
     * 
     * @return 账号列表 Account list
     */
    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }
    
    /**
     * 获取选中的账号
     * Get selected account
     * 
     * @return 选中的账号 Selected account
     */
    public Account getSelectedAccount() {
        return selectedAccount;
    }
    
    /**
     * 刷新账号令牌
     * Refresh account token
     * 
     * @param account 账号 Account
     * @return 异步操作结果 Async operation result
     */
    public CompletableFuture<Boolean> refreshToken(Account account) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: 实现令牌刷新逻辑 Implement token refresh logic
                return true;
            } catch (Exception e) {
                LogUtils.error("刷新令牌失败 Failed to refresh token", e);
                return false;
            }
        });
    }
    
    /**
     * 验证账号
     * Validate account
     * 
     * @param account 账号 Account
     * @return 异步操作结果 Async operation result
     */
    public CompletableFuture<Boolean> validateAccount(Account account) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: 实现账号验证逻辑 Implement account validation logic
                return true;
            } catch (Exception e) {
                LogUtils.error("验证账号失败 Failed to validate account", e);
                return false;
            }
        });
    }
} 