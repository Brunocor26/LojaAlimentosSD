async function verificarAutenticacao() {
    try {
        const r = await fetch('/api/auth/me');
        if (r.ok) {
            const user = await r.json();
            localStorage.setItem('user_role', user.role);
            localStorage.setItem('user_name', user.nome);
            return user;
        }
        localStorage.removeItem('user_role');
        localStorage.removeItem('user_name');
        return null;
    } catch {
        return null;
    }
}

async function fazerLogout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    localStorage.removeItem('user_role');
    localStorage.removeItem('user_name');
    window.location.href = '/index.html';
}
