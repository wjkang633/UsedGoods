package woojin.android.kotlin.project.usedgoods.mypage

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import woojin.android.kotlin.project.usedgoods.R
import woojin.android.kotlin.project.usedgoods.databinding.FragmentMyPageBinding

class MyPageFragment : Fragment(R.layout.fragment_my_page) {

    private var binding: FragmentMyPageBinding? = null

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentMyPageBinding = FragmentMyPageBinding.bind(view)
        binding = fragmentMyPageBinding

        fragmentMyPageBinding.signInOutButton.setOnClickListener {
            binding?.let {
                val email = it.emailEditText.text.toString()
                val password = it.passwordEditText.text.toString()

                if (auth.currentUser == null) {
                    //로그인
                    auth
                            .signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(requireActivity()) { task ->
                                if (task.isSuccessful) {
                                    successSignIn()
                                } else {
                                    Toast.makeText(context, "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            }
                } else {
                    //로그아웃
                    auth.signOut()

                    it.emailEditText.text.clear()
                    it.emailEditText.isEnabled = false
                    it.passwordEditText.text.clear()
                    it.passwordEditText.isEnabled = false

                    it.signInOutButton.text = "로그인"
                    it.signInOutButton.isEnabled = false
                    it.signUpButton.isEnabled = false
                }
            }
        }

        fragmentMyPageBinding.signUpButton.setOnClickListener {
            binding?.let {
                val email = it.emailEditText.text.toString()
                val password = it.passwordEditText.text.toString()

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "회원가입에 성공하였습니다. 로그인해주세요.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            Log.e("우진", "${it}")
                        }
            }
        }

        fragmentMyPageBinding.emailEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
                binding.signInOutButton.isEnabled = enable
                binding.signUpButton.isEnabled = enable
            }
        }

        fragmentMyPageBinding.passwordEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
                binding.signInOutButton.isEnabled = enable
                binding.signUpButton.isEnabled = enable
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser == null) {
            binding?.let {
                it.emailEditText.text.clear()
                it.emailEditText.isEnabled = true
                it.passwordEditText.text.clear()
                it.passwordEditText.isEnabled = true
                it.signInOutButton.text = "로그인"
                it.signInOutButton.isEnabled = false
                it.signUpButton.isEnabled = false
            }
        } else {
            binding?.let {
                it.emailEditText.setText(auth.currentUser!!.email)
                it.emailEditText.isEnabled = false
                it.passwordEditText.setText("********")
                it.passwordEditText.isEnabled = false
                it.signInOutButton.text = "로그아웃"
                it.signInOutButton.isEnabled = true
                it.signUpButton.isEnabled = false
            }
        }
    }

    private fun successSignIn() {
        if (auth.currentUser == null) {
            Toast.makeText(context, "로그인에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        binding?.emailEditText?.isEnabled = false
        binding?.passwordEditText?.isEnabled = false
        binding?.signUpButton?.isEnabled = false
        binding?.signInOutButton?.text = "로그아웃"
    }
}