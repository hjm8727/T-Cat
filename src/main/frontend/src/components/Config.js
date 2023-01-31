export const REDIRECT_URI = 'http://tcat.pe.kr/login/oauth2/code/kakao';
export const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${process.env.REACT_APP_REST_API_KEY}&redirect_uri=${REDIRECT_URI}&response_type=code&prompt=login`;
export const GOOGLE_URL = 'https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?scope=profile%20email%20openid&response_type=code&redirect_uri=https%3A%2F%2Ftcats.tk%2Fgoogle%2Flogin%2Fredirect&client_id=378869361700-4fvmngl6ce5b8d690gno3s3kgnf7eoku.apps.googleusercontent.com&service=lso&o2v=2&flowName=GeneralOAuthFlow';
