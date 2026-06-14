package com.example.focusplay.auth

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.focusplay.R
import com.example.focusplay.profile.PilihPeranActivity
import com.example.focusplay.utils.AuthBottomSheetHelper
import com.example.focusplay.utils.ErrorDialogHelper
import com.example.focusplay.utils.LoadingDialogHelper
import com.example.focusplay.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterBottomSheetFragment : BottomSheetDialogFragment(R.layout.fragment_register_bottom_sheet) {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var session: SessionManager
    private lateinit var loadingDialog: LoadingDialogHelper

    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnDaftar: View
    private lateinit var btnRegisterGoogle: View
    private lateinit var tvMasukSini: TextView

    private var passwordTerlihat = false
    private var prosesGoogleBerjalan = false

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        prosesGoogleBerjalan = false

        if (result.resultCode != Activity.RESULT_OK) {
            tampilkanError(
                "Daftar Google Dibatalkan",
                "Pilih akun Google untuk melanjutkan pendaftaran."
            )
            return@registerForActivityResult
        }

        loadingDialog.show()

        try {
            val account = GoogleSignIn
                .getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrBlank()) {
                loadingDialog.dismiss()
                tampilkanError("Daftar Google Gagal", "Token Google tidak ditemukan.")
            } else {
                autentikasiGoogle(idToken)
            }
        } catch (e: ApiException) {
            loadingDialog.dismiss()
            tampilkanError(
                "Daftar Google Gagal",
                "Akun Google belum berhasil diproses. Kode: ${e.statusCode}"
            )
        }
    }

    override fun getTheme(): Int {
        return R.style.FocusPlayBottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        AuthBottomSheetHelper.setup(dialog)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        session = SessionManager(requireContext())
        loadingDialog = LoadingDialogHelper(requireActivity())

        hubungkanView(view)
        setupGoogleSignIn()
        aturAksiTombol()
    }

    private fun hubungkanView(view: View) {
        etNama = view.findViewById(R.id.etNamaRegister)
        etEmail = view.findViewById(R.id.etEmailRegister)
        etPassword = view.findViewById(R.id.etPasswordRegister)
        btnTogglePassword = view.findViewById(R.id.btnTogglePasswordRegister)
        btnDaftar = view.findViewById(R.id.btnProsesRegister)
        btnRegisterGoogle = view.findViewById(R.id.btnRegisterGoogle)
        tvMasukSini = view.findViewById(R.id.tvMasukSini)
    }

    private fun aturAksiTombol() {
        tvMasukSini.setOnClickListener {
            bukaLoginBottomSheet()
        }

        btnTogglePassword.setOnClickListener {
            passwordTerlihat = !passwordTerlihat
            aturTampilanPassword(etPassword, btnTogglePassword, passwordTerlihat)
        }

        btnDaftar.setOnClickListener {
            validasiDanDaftar()
        }

        btnRegisterGoogle.setOnClickListener {
            if (prosesGoogleBerjalan) return@setOnClickListener
            prosesGoogleBerjalan = true

            googleSignInClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    }

    private fun setupGoogleSignIn() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), options)
    }

    private fun autentikasiGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                loadingDialog.dismiss()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    session.simpanSesiLogin(
                        idPendamping = 0,
                        namaPendamping = user?.displayName ?: "Pengguna",
                        email = user?.email.orEmpty()
                    )

                    val intent = Intent(requireContext(), PilihPeranActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    dismissAllowingStateLoss()
                } else {
                    tampilkanError(
                        "Daftar Google Gagal",
                        task.exception?.localizedMessage
                            ?: "Akun Google belum berhasil didaftarkan."
                    )
                }
            }
    }

    private fun bukaLoginBottomSheet() {
        val fragmentManager = parentFragmentManager
        dismissAllowingStateLoss()

        Handler(Looper.getMainLooper()).postDelayed({
            if (!fragmentManager.isStateSaved &&
                fragmentManager.findFragmentByTag("LoginBottomSheet") == null
            ) {
                LoginBottomSheetFragment().show(fragmentManager, "LoginBottomSheet")
            }
        }, 180L)
    }

    private fun validasiDanDaftar() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

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
                                bukaLoginBottomSheet()

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
