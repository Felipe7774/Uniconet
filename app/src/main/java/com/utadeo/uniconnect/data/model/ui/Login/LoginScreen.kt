package com.utadeo.uniconnect.data.model.ui.Login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens

@Composable
fun LoginAndRegister(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(R.color.primary_color)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(1.dp))


            Box(
                modifier = Modifier
                    .offset(y = -30.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "U",
                    fontSize = 200.sp,
                    fontFamily = FontFamily(Font(R.font.crimsontext_regular)),
                    color = colorResource(R.color.white),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "U",
                    fontSize = 200.sp,
                    fontFamily = FontFamily(Font(R.font.crimsontext_regular)),
                    color = colorResource(R.color.black),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 10.dp)
                )


                Text(
                    text = "C",
                    fontSize = 200.sp,
                    fontFamily = FontFamily(Font(R.font.crimsontext_regular)),
                    color = colorResource(R.color.white),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 300.dp)
                )
                Text(
                    text = "C",
                    fontSize = 200.sp,
                    fontFamily = FontFamily(Font(R.font.crimsontext_regular)),
                    color = colorResource(R.color.black),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 300.dp)
                        .padding(start = 10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {

                Button(
                    onClick = { navController.navigate(AppScreens.LoginWithPasswordScreen.route) },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.white),
                        contentColor = colorResource(R.color.primary_color)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        color = colorResource(R.color.black),
                    )
                }

                Button(
                    onClick = { navController.navigate(AppScreens.RegisterScreen.route) },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.white),
                        contentColor = colorResource(R.color.primary_color)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                ) {
                    Text(
                        text = "Registrarse",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        color = colorResource(R.color.black),
                    )
                }
            }



        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginAndRegisterPreview() {
    val navController = rememberNavController()
    LoginAndRegister(navController)
}

