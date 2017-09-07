package tr.edu.iyte.quickreply.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import org.jetbrains.anko.*
import tr.edu.iyte.quickreply.R
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_reply_rule.*
import tr.edu.iyte.quickreply.fragments.NewReplyFragment
import tr.edu.iyte.quickreply.fragments.ReplyFragment
import tr.edu.iyte.quickreply.helper.Constants

class ReplyRuleActivity :
        AppCompatActivity(),
        NewReplyFragment.OnAddReplyInteractionListener,
        AnkoLogger {
    private val PERMISSION_REQUEST_CODE = 1
    private var isRuleFragmentPresent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply_rule)

        val permissions = arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS)
        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
            return
        }

        // simulate back button
        back_button.setOnClickListener {
            onBackPressed()
        }

        change_reply_rule_button.setOnClickListener {
            if(isRuleFragmentPresent) {
                //supportFragmentManager.beginTransaction().replace(R.id.reply_rule_fragment, RuleFragment.newInstance()).commitNow()
            } else
                putReplyFragment()
            TODO("manage fragments")
        }

        settings_button.setOnClickListener {
            startActivity<SettingsActivity>()
        }

        supportFragmentManager
                .beginTransaction()
                .add(R.id.reply_rule_fragment,
                        ReplyFragment(),
                        Constants.REPLY_FRAGMENT_TAG)
                .commit()
    }

    override fun onNewReplySaved(reply: String) {
        val replyFragment = supportFragmentManager.findFragmentByTag(Constants.REPLY_FRAGMENT_TAG) as ReplyFragment
        replyFragment.addReply(reply)
    }

    private fun putReplyFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.reply_rule_fragment,
                        ReplyFragment(),
                        Constants.REPLY_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var shouldCheck = true
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    warn("Permission ${permissions[i]} denied")
                    shouldCheck = false
                } else
                    info("Permission ${permissions[i]} granted")
            }

            if (!shouldCheck) {
                toast(getString(R.string.permission_denied))
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}