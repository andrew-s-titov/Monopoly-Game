import { useAuthContext } from "../context/AuthContextProvider";
import LoginPage from "./LoginPage";
import LandingPage from "./LandingPage";

const StartPage = () => {

  const { isLoggedIn } = useAuthContext();

  return (
    isLoggedIn
      ? <LandingPage/>
      : <LoginPage/>
  );
}

export default StartPage;
