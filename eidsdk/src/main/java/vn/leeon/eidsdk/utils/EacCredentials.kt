package vn.leeon.eidsdk.utils

import java.security.PrivateKey
import java.security.cert.Certificate

/**
 * Encapsulates the terminal key and associated certificate chain for terminal authentication.
 */
class EacCredentials
/**
 * Creates EAC credentials.
 *
 * @param privateKey
 * @param chain
 */
(val privateKey: PrivateKey, val chain: Array<Certificate>)
