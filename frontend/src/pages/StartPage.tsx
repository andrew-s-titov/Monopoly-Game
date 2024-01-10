import LoginForm from "./LoginPage";
import { useAuthContext } from "../context/AuthContextProvider";
import LandingPage from "./LandingPage";

const StartPage = () => {

  const { isLoggedIn } = useAuthContext();

  return (
    isLoggedIn
      ? <LandingPage/>
      : <LoginForm/>
  );
}

export default StartPage;
