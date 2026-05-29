package com.example.focusplay.auth

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.focusplay.R
import com.example.focusplay.utils.ErrorDialogHelper
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
    private lateinit var btnDaftar: View
    private lateinit var btnRegisterGoogle: View
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
                val sheetHeight = (screenHeight * 0.80).toInt()

                sheet.layoutParams.height = sheetHeight
                sheet.requestLayout()

                BottomSheetBehavior.from(sheet).apply {
                    isDraggable = true
                    isHideable = true
                    skipCollapsed = true
                    peekHeight = sheetHeight
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
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
        btnRegisterGoogle = view.findViewById(R.id.btnRegisterGoogle)
        tvMasukSini = view.findViewById(R.id.tvMasukSini)
    }

    private fun aturAksiTombol() {
        tvMasukSini.setOnClickListener {
            dismiss()
            LoginBottomSheetFragment().show(parentFragmentManager, "LoginBottomSheet")
        }

        btnTogglePassword.setOnClickListener {
            passwordTerlihat = !passwordTerlihat
            aturTampilanPassword(etPassword, btnTogglePassword, passwordTerlihat)
        }

        btnToggleKonfirmasiPassword.setOnClickListener {
            konfirmasiPasswordTerlihat = !konfirmasiPasswordTerlihat
            aturTampilanPassword(
                etKonfirmasiPassword,
                btnToggleKonfirmasiPassword,
                konfirmasiPasswordTerlihat
            )
        }

        btnDaftar.setOnClickListener {
            validasiDanDaftar()
        }

        btnRegisterGoogle.setOnClickListener {
            dismiss()
            LoginBottomSheetFragment().show(parentFragmentManager, "LoginBottomSheet")
        }
    }

    private fun validasiDanDaftar() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val konfirmasiPassword = etKonfirmasiPassword.text.toString().trim()

        if (nama.isEmpty()) {
            tampilkanError("Nama Kosong", "Nama wajib diisi terlebih dahulu.")
            etNama.requestFocus()
            return
        }

        if (email.isEmpty()) {
            tampilkanError("Email Kosong", "Email wajib diisi terlebih dahulu.")
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tampilkanError("Email Tidak Valid", "Format email belum benar.")
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            tampilkanError("Password Kosong", "Password wajib diisi terlebih dahulu.")
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            tampilkanError("Password Terlalu Pendek", "Password minimal 6 karakter.")
            etPassword.requestFocus()
            return
        }

        if (konfirmasiPassword.isEmpty()) {
            tampilkanError("Konfirmasi Password Kosong", "Konfirmasi password wajib diisi terlebih dahulu.")
            etKonfirmasiPassword.requestFocus()
            return
        }

        if (password != konfirmasiPassword) {
            tampilkanError("Password Tidak Sama", "Konfirmasi password harus sama dengan password.")
            etKonfirmasiPassword.requestFocus()
            return
        }

        prosesRegisterFirebase(nama, email, password)
    }

    private fun prosesRegisterFirebase(nama: String, email: String, password: String) {
        btnDaftar.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                btnDaftar.isEnabled = true

                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nama)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {

                                auth.signOut()
                                dismiss()

                                LoginBottomSheetFragment().show(
                                    parentFragmentManager,
                                    "LoginBottomSheet"
                                )

                            } else {
                                tampilkanError(
                                    "Pendaftaran Gagal",
                                    "Akun berhasil dibuat, tetapi nama pengguna belum berhasil disimpan."
                                )
                            }
                        }

                } else {
                    tampilkanError(
                        "Pendaftaran Gagal",
                        task.exception?.message ?: "Akun belum berhasil dibuat. Coba lagi."
                    )
                }
            }
    }

    private fun aturTampilanPassword(editText: EditText, button: ImageButton, terlihat: Boolean) {
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