package com.example.focusplay.auth

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.focusplay.R
import com.example.focusplay.utils.ErrorDialogHelper
import com.example.focusplay.utils.SuccessDialogHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterBottomSheetFragment : BottomSheetDialogFragment(R.layout.fragment_register_bottom_sheet) {

    private lateinit var auth: FirebaseAuth

    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etKonfirmasiPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleKonfirmasiPassword: ImageButton
    private lateinit var btnDaftar: Button
    private lateinit var tvMasukSini: TextView

    private var passwordTerlihat = false
    private var konfirmasiPasswordTerlihat = false

    override fun getTheme(): Int {
        return R.style.FocusPlayBottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            bottomSheet?.let { sheet ->
                val screenHeight = resources.displayMetrics.heightPixels

                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                sheet.requestLayout()

                val behavior = BottomSheetBehavior.from(sheet)

                behavior.isDraggable = true
                behavior.isHideable = false
                behavior.skipCollapsed = false

                // Register form lebih panjang, jadi peekHeight jangan terlalu kecil.
                behavior.peekHeight = (screenHeight * 0.80).toInt()

                // Saat pertama muncul langsung dalam posisi penuh.
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        hubungkanView(view)
        aturAksiTombol()
    }

    private fun hubungkanView(view: View) {
        etNama = view.findViewById(R.id.etNamaRegister)
        etEmail = view.findViewById(R.id.etEmailRegister)
        etPassword = view.findViewById(R.id.etPasswordRegister)
        etKonfirmasiPassword = view.findViewById(R.id.etKonfirmasiPasswordRegister)
        btnTogglePassword = view.findViewById(R.id.btnTogglePasswordRegister)
        btnToggleKonfirmasiPassword = view.findViewById(R.id.btnToggleKonfirmasiPasswordRegister)
        btnDaftar = view.findViewById(R.id.btnProsesRegister)
        tvMasukSini = view.findViewById(R.id.tvMasukSini)
    }

    private fun aturAksiTombol() {
        tvMasukSini.setOnClickListener {
            dismiss()

            LoginBottomSheetFragment().show(
                parentFragmentManager,
                "LoginBottomSheet"
            )
        }

        btnTogglePassword.setOnClickListener {
            passwordTerlihat = !passwordTerlihat
            aturTampilanPassword(
                editText = etPassword,
                button = btnTogglePassword,
                terlihat = passwordTerlihat
            )
        }

        btnToggleKonfirmasiPassword.setOnClickListener {
            konfirmasiPasswordTerlihat = !konfirmasiPasswordTerlihat
            aturTampilanPassword(
                editText = etKonfirmasiPassword,
                button = btnToggleKonfirmasiPassword,
                terlihat = konfirmasiPasswordTerlihat
            )
        }

        btnDaftar.setOnClickListener {
            validasiDanDaftar()
        }
    }

    private fun validasiDanDaftar() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val konfirmasiPassword = etKonfirmasiPassword.text.toString().trim()

        if (nama.isEmpty()) {
            tampilkanError(
                title = "Nama Kosong",
                message = "Nama wajib diisi terlebih dahulu."
            )
            etNama.requestFocus()
            return
        }

        if (email.isEmpty()) {
            tampilkanError(
                title = "Email Kosong",
                message = "Email wajib diisi terlebih dahulu."
            )
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tampilkanError(
                title = "Email Tidak Valid",
                message = "Format email belum benar."
            )
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            tampilkanError(
                title = "Password Kosong",
                message = "Password wajib diisi terlebih dahulu."
            )
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            tampilkanError(
                title = "Password Terlalu Pendek",
                message = "Password minimal 6 karakter."
            )
            etPassword.requestFocus()
            return
        }

        if (konfirmasiPassword.isEmpty()) {
            tampilkanError(
                title = "Konfirmasi Password Kosong",
                message = "Konfirmasi password wajib diisi terlebih dahulu."
            )
            etKonfirmasiPassword.requestFocus()
            return
        }

        if (password != konfirmasiPassword) {
            tampilkanError(
                title = "Password Tidak Sama",
                message = "Konfirmasi password harus sama dengan password."
            )
            etKonfirmasiPassword.requestFocus()
            return
        }

        prosesRegisterFirebase(nama, email, password)
    }

    private fun prosesRegisterFirebase(nama: String, email: String, password: String) {
        btnDaftar.isEnabled = false
        btnDaftar.text = "Memproses..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                btnDaftar.isEnabled = true
                btnDaftar.text = "Daftar Sekarang"

                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nama)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                SuccessDialogHelper.showSuccessDialog(
                                    activity = requireActivity(),
                                    title = "Pendaftaran Berhasil!",
                                    message = "Akun berhasil dibuat. Silakan masuk menggunakan email dan password kamu."
                                ) {
                                    auth.signOut()
                                    dismiss()

                                    LoginBottomSheetFragment().show(
                                        parentFragmentManager,
                                        "LoginBottomSheet"
                                    )
                                }
                            } else {
                                tampilkanError(
                                    title = "Pendaftaran Gagal",
                                    message = "Akun berhasil dibuat, tetapi nama pengguna belum berhasil disimpan."
                                )
                            }
                        }

                } else {
                    val pesanError = when {
                        task.exception?.message?.contains("email address is already in use", true) == true -> {
                            "Email ini sudah digunakan. Silakan gunakan email lain atau masuk dengan akun tersebut."
                        }

                        task.exception?.message?.contains("badly formatted", true) == true -> {
                            "Format email belum benar."
                        }

                        task.exception?.message?.contains("password", true) == true -> {
                            "Password belum sesuai. Gunakan minimal 6 karakter."
                        }

                        else -> {
                            task.exception?.message ?: "Akun belum berhasil dibuat. Coba lagi."
                        }
                    }

                    tampilkanError(
                        title = "Pendaftaran Gagal",
                        message = pesanError
                    )
                }
            }
    }

    private fun aturTampilanPassword(
        editText: EditText,
        button: ImageButton,
        terlihat: Boolean
    ) {
        if (terlihat) {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            button.setImageResource(R.drawable.ic_eye_off)
        } else {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button.setImageResource(R.drawable.ic_eye)
        }

        editText.setSelection(editText.text.length)
    }

    private fun tampilkanError(title: String, message: String) {
        ErrorDialogHelper.showErrorDialog(
            activity = requireActivity(),
            title = title,
            message = message
        )
    }
}