package com.frx.jetpro.ui.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.frx.jetpro.R;
import com.frx.jetpro.databinding.ActivityLayoutLoginBinding;
import com.frx.jetpro.model.User;
import com.frx.libcommon.utils.StatusBar;

public class LoginActivity extends AppCompatActivity {

    private ActivityLayoutLoginBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        mBinding = ActivityLayoutLoginBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.actionClose.setOnClickListener(v -> {
            finish();
        });

        mBinding.actionLogin.setOnClickListener(v -> {
            if (checkUserInfo()) {
                Toast.makeText(this, "正在登陆...", Toast.LENGTH_SHORT).show();
                mBinding.loading.start();
                loginApp();
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void loginApp() {
        ArchTaskExecutor.getInstance().executeOnDiskIO(() -> {
            try {
                Thread.sleep(5000);
                User user = new User();
                user.avatar = "https://p3-dy.byteimg" +
                        ".com/img/p1056/8c50025c85244140910a513345ae7358~200x200.webp";
                user.name = "二师弟请随我来";
                user.userId = "3223400206308231";
                user.id = 962;
                user.qqOpenId = System.currentTimeMillis() + "";
                //保存用户数据
                UserManager.get().save(user);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "登陆成功", Toast.LENGTH_SHORT).show();
                mBinding.loading.stop();
            });
            try {
                Thread.sleep(500);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean checkUserInfo() {
        if (mBinding.etUserName.getText().toString().isEmpty()) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mBinding.etUserPsw.getText().toString().isEmpty()) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}
