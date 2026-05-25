package com.example.focusplay.view.auth

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            bottomSheet?.let { sheet ->
                sheet.layoutParams.height = (resources.displayMetrics.heightPixels * 0.65).toInt()
                sheet.requestLayout()

                val behavior = BottomSheetBehavior.from(sheet)

                behavior.isDraggable = true
                behavior.skipCollapsed = true
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
    }

    private fun validasiDanDaftar() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val konfirmasiPassword = etKonfirmasiPassword.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama wajib diisi"
            etNama.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email wajib diisi"
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password wajib diisi"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            etPassword.requestFocus()
            return
        }

        if (konfirmasiPassword.isEmpty()) {
            etKonfirmasiPassword.error = "Konfirmasi password wajib diisi"
            etKonfirmasiPassword.requestFocus()
            return
        }

        if (password != konfirmasiPassword) {
            etKonfirmasiPassword.error = "Konfirmasi password tidak sama"
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
                        ?.addOnCompleteListener {
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

                    ErrorDialogHelper.showErrorDialog(
                        activity = requireActivity(),
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

    override fun getTheme(): Int {
        return R.style.FocusPlayBottomSheetDialog
    }
}