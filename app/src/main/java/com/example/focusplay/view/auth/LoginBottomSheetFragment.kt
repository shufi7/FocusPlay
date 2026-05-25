package com.example.focusplay.view.auth

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.focusplay.R
import com.example.focusplay.utils.ErrorDialogHelper
import com.example.focusplay.utils.LoadingDialogHelper
import com.example.focusplay.utils.SessionManager
import com.example.focusplay.utils.SuccessDialogHelper
import com.example.focusplay.view.PilihPeranActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginBottomSheetFragment : BottomSheetDialogFragment(R.layout.fragment_login_bottom_sheet) {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var session: SessionManager
    private lateinit var loadingDialog: LoadingDialogHelper

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageButton

    private lateinit var btnLogin: View
    private lateinit var btnRegister: TextView
    private lateinit var btnLoginGoogle: View

    private var passwordTerlihat = false

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == android.app.Activity.RESULT_OK) {
            loadingDialog.show()

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    loadingDialog.dismiss()
                    tampilkanError(
                        title = "Login Gagal",
                        message = "Token Google tidak ditemukan. Coba ulangi lagi ya."
                    )
                }

            } catch (e: ApiException) {
                loadingDialog.dismiss()
                tampilkanError(
                    title = "Google Sign-In Gagal",
                    message = "Terjadi masalah saat memilih akun Google. Coba ulangi lagi ya."
                )
            }

        } else {
            loadingDialog.dismiss()
            tampilkanError(
                title = "Login Dibatalkan",
                message = "Kamu belum memilih akun Google untuk masuk."
            )
        }
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
                behavior.peekHeight = (screenHeight * 0.65).toInt()
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        session = SessionManager(requireContext())
        loadingDialog = LoadingDialogHelper(requireActivity())

        hubungkanView(view)
        setupGoogleLogin()
        aturAksiTombol()
    }

    private fun hubungkanView(view: View) {
        etEmail = view.findViewById(R.id.etEmailLogin)
        etPassword = view.findViewById(R.id.etPasswordLogin)
        btnTogglePassword = view.findViewById(R.id.btnTogglePasswordLogin)

        btnLogin = view.findViewById(R.id.btnProsesLogin)
        btnRegister = view.findViewById(R.id.tvDaftarSini)
        btnLoginGoogle = view.findViewById(R.id.btnLoginGoogle)
    }

    private fun setupGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun aturAksiTombol() {
        btnLogin.setOnClickListener {
            prosesLoginEmailPassword()
        }

        btnRegister.setOnClickListener {
            dismiss()

            RegisterBottomSheetFragment().show(
                parentFragmentManager,
                "RegisterBottomSheet"
            )
        }

        btnLoginGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                launcher.launch(googleSignInClient.signInIntent)
            }
        }

        btnTogglePassword.setOnClickListener {
            passwordTerlihat = !passwordTerlihat
            aturTampilanPassword()
        }
    }

    private fun prosesLoginEmailPassword() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            tampilkanError(
                title = "Email Kosong",
                message = "Email wajib diisi terlebih dahulu."
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

        loadingDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                loadingDialog.dismiss()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val namaUser = user?.displayName ?: ambilNamaDariEmail(user?.email ?: email)
                    val emailUser = user?.email ?: email

                    session.simpanSesiLogin(
                        0,
                        namaUser,
                        emailUser
                    )

                    SuccessDialogHelper.showSuccessDialog(
                        activity = requireActivity(),
                        title = "Login Berhasil!",
                        message = "Selamat datang di FocusPlay!"
                    ) {
                        bukaPilihPeran()
                    }

                } else {
                    tampilkanError(
                        title = "Login Gagal",
                        message = "Email atau password belum sesuai. Coba periksa kembali ya."
                    )
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                loadingDialog.dismiss()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val namaUser = user?.displayName ?: "Pengguna"
                    val emailUser = user?.email ?: ""

                    session.simpanSesiLogin(
                        0,
                        namaUser,
                        emailUser
                    )

                    SuccessDialogHelper.showSuccessDialog(
                        activity = requireActivity(),
                        title = "Login Berhasil!",
                        message = "Selamat datang, ${ambilNamaPanggilan(namaUser)}!"
                    ) {
                        bukaPilihPeran()
                    }

                } else {
                    tampilkanError(
                        title = "Login Gagal",
                        message = "Akun belum berhasil masuk. Periksa koneksi internet atau coba lagi ya."
                    )
                }
            }
    }

    private fun aturTampilanPassword() {
        if (passwordTerlihat) {
            etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        } else {
            etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye)
        }

        etPassword.setSelection(etPassword.text.length)
    }

    private fun ambilNamaPanggilan(namaLengkap: String): String {
        return namaLengkap
            .trim()
            .split(" ")
            .firstOrNull()
            ?: "Pengguna"
    }

    private fun ambilNamaDariEmail(email: String): String {
        return email
            .substringBefore("@")
            .replace(".", " ")
            .replace("_", " ")
            .trim()
            .replaceFirstChar { it.uppercase() }
    }

    private fun bukaPilihPeran() {
        val intent = Intent(requireContext(), PilihPeranActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun tampilkanError(title: String, message: String) {
        ErrorDialogHelper.showErrorDialog(
            activity = requireActivity(),
            title = title,
            message = message
        )
    }

    override fun getTheme(): Int {
        return R.style.FocusPlayBottomSheetDialog
    }
}