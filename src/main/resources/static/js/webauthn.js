async function registerAsync() {
    try {
        const optionsRes = await postAttestationOptions();
        const optionsJSON = await optionsRes.json();
        const credential = await createCredential(optionsJSON);
        const response = await registerFinish(credential);
        redirectToSignInPage(response)
    } catch (error) {
        alert(error)
    }
}

function postAttestationOptions() {
    const url = '/attestation/options'
    const data = {
        'email' : document.getElementById('email').value
    }

    return fetch(url, {
        method: 'POST',
        body: JSON.stringify(data),
        headers: {
            'Content-Type': 'application/json'
        }
    })
}

function createCredential(options) {
    options.challenge = stringToArrayBuffer(options.challenge.value);
    options.user.id = stringToArrayBuffer(options.user.id);
    options.excludeCredentials =
        options.excludeCredentials.map(credential => Object.assign({},
            credential, {
                id: base64ToArrayBuffer(credential.id)
            }));
    return navigator.credentials.create({
        'publicKey': options
    })
}

function registerFinish(credential) {
    const url = '/attestation/result'
    const data = {
        'clientDataJSON': arrayBufferToBase64(
            credential.response.clientDataJSON),
        'attestationObject': arrayBufferToBase64(
            credential.response.attestationObject)
    };
    
    return fetch(url, {
        method: 'POST',
        body: JSON.stringify(data),
        headers: {
            'Content-Type': 'application/json'
        }
    })
}

function redirectToSignInPage(response) {
    console.log(response)
    location.href = 'signin.html'
}

// 文字列をArrayBufferに変換
function stringToArrayBuffer(string) {
    return new TextEncoder().encode(string);
}

// Base64文字列をArrayBufferにデコード
function base64ToArrayBuffer(base64String) {
    return Uint8Array.from(atob(base64String), c => c.charCodeAt(0));
}

// ArrayBufferをBase64文字列にエンコード
function arrayBufferToBase64(arrayBuffer) {
    return btoa(String.fromCharCode(...new Uint8Array(arrayBuffer)));
}