package com.example.doan.util

import android.util.Patterns

/* Đặt trong một tệp vì ta sẽ sử dụng nó trong đoạn đăng nhập
* và ta sẽ không viết lại chức năng này */


/**
 * This method is using to validate Email
 * */
fun validateEmail(email: String): RegisterValidation {
    if (email.isEmpty()) {
        return RegisterValidation.Failed("Email không thể để trống.")
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return RegisterValidation.Failed("Sai định dạng email.")
    }

    return RegisterValidation.Success
}

/**
 * This method is using to validate password
 * */
fun validatePassword(password: String): RegisterValidation {
    if (password.isEmpty()) {
        return RegisterValidation.Failed("Mật khẩu không thể để trống.")
    }

    if (password.length < 6) {
        return RegisterValidation.Failed("Độ dài mật khẩu phải lớn hơn 6 ký tự.")
    }

    return RegisterValidation.Success
}
