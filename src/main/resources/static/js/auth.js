async function verificarAutenticacao() {
    try {
        const r = await fetch('/api/auth/me');
        return r.ok ? await r.json() : null;
    } catch {
        return null;
    }
}

async function fazerLogout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    window.location.href = '/index.html';
}
