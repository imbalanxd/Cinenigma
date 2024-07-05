package com.imbaland.movies.ui.widget

import android.util.Range
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.test.internal.util.LogUtil.logDebug
import com.imbaland.common.tool.logDebug
import com.imbaland.common.ui.UrlImage
import com.imbaland.movies.R
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.Person
import com.imbaland.movies.ui.util.poster

@Composable
fun MovieCast(
    modifier: Modifier = Modifier,
    cast: List<Person>,
    onSubmit: ((Int, Int) -> Unit)? = null
) {
    LazyRow(modifier = modifier) {
        itemsIndexed(cast) { castId, cast ->
            CastMember(
                modifier = Modifier.padding(5.dp).clip(RoundedCornerShape(5.dp)),
                person = cast,
                onSubmit = onSubmit?.let { submit ->
                    { movieId -> submit.invoke(castId, movieId) }
                })
        }
    }
}

@OptIn(ExperimentalMotionApi::class)
@Preview
@Composable
fun CastMember(
    modifier: Modifier = Modifier,
    person: Person = Person(
        filmography = listOf(
            Movie(),
            Movie(),
            Movie(),
            Movie(),
            Movie(),
            Movie(),
            Movie(),
            Movie()
        )
    ),
    onSubmit: ((Int) -> Unit)? = null
) {
    var selection by rememberSaveable { mutableIntStateOf(-1) }
    var selectionActive by rememberSaveable { mutableStateOf(false) }
    val buttonAnimationProgress by animateFloatAsState(
        targetValue = if (selectionActive) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = EaseOutQuart)
    )
    val constraintSetUnselected = remember {
        ConstraintSet {
            val profile = createRefFor("profile")
            val selectedMovieFullRes = createRefFor("movieFullRes")
            val submit = createRefFor("submit")
            val movieRefs = List(person.filmography.size) { index -> createRefFor("movie$index") }
            val movieGuidelines = List(person.filmography.size - 1) { index ->
                createGuidelineFromTop((1f / person.filmography.size) * (index + 1))
            }
            constrain(profile) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
            }
            constrain(selectedMovieFullRes) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
            }
            constrain(submit) {
                top.linkTo(parent.top)
                end.linkTo(parent.start)
            }
            movieRefs.forEachIndexed { index, movie ->
                val range = Range(0, movieRefs.size - 1)
                constrain(movie) {
                    start.linkTo(profile.end)
                    end.linkTo(parent.end)
                    if (range.contains(index - 1)) {
                        top.linkTo(movieGuidelines[index - 1])
                    } else {
                        top.linkTo(profile.top)
                    }
                    if (range.contains(index + 1)) {
                        bottom.linkTo(movieGuidelines[index])
                    } else {
                        bottom.linkTo(profile.bottom)
                    }
                    height = Dimension.fillToConstraints
                }
            }
        }
    }
    val constraintSetSelected = remember(selection) {
        ConstraintSet {
            val profile = createRefFor("profile")
            val selectedMovieFullRes = createRefFor("movieFullRes")
            val submit = createRefFor("submit")
            val movieRefs = List(person.filmography.size) { index -> createRefFor("movie$index") }
            val movieGuidelines = List(person.filmography.size - 1) { index ->
                createGuidelineFromTop((1f / person.filmography.size) * (index + 1))
            }
            constrain(profile) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
            }
            constrain(selectedMovieFullRes) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            if (selectionActive) {
                constrain(submit) {
                    top.linkTo(movieRefs[selection].top, 3.dp)
                    start.linkTo(movieRefs[selection].start, 3.dp)
                }
            }
            movieRefs.forEachIndexed { index, movie ->
                val range = Range(0, movieRefs.size - 1)
                constrain(movie) {
                    if (index == selection) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.percent(0.9f)
                    } else {
                        start.linkTo(profile.end)
                        end.linkTo(parent.end)
                        if (range.contains(index - 1)) {
                            top.linkTo(movieGuidelines[index - 1])
                        } else {
                            top.linkTo(profile.top)
                        }
                        if (range.contains(index + 1)) {
                            bottom.linkTo(movieGuidelines[index])
                        } else {
                            bottom.linkTo(profile.bottom)
                        }
                        height = Dimension.fillToConstraints
                    }
                }
            }
        }
    }
    MotionLayout(
        start = constraintSetUnselected, end = constraintSetSelected,
        modifier = modifier, progress = buttonAnimationProgress
    ) {
        UrlImage(modifier = Modifier.layoutId("profile").zIndex(1f).poster(), url = person.image)
        with(person.filmography) {
            repeat(size) { index ->
                val isSelected = selection == index
                UrlImage(
                    modifier = Modifier.zIndex(if (isSelected) 2f else 1f).layoutId("movie$index")
                        .poster().clickable {
                        if (selectionActive) {
                            selectionActive = false
                        } else {
                            selectionActive = true
                            selection = index
                        }
                    }
                        .clip(RoundedCornerShape(if (isSelected) 5.dp * buttonAnimationProgress else 0.dp)),
                    url = get(index).image,
                    contentScale = ContentScale.Crop,
                    adjustSize = false
                )
            }
        }
        onSubmit?.let { submit ->
            IconButton(
                modifier = Modifier.layoutId("submit").size(30.dp).alpha(buttonAnimationProgress)
                    .zIndex(3f),
                onClick = { submit.invoke(selection) }
            ) {
                Icon(
                    modifier = Modifier,
                    painter = painterResource(R.drawable.ic_confirm),
                    contentDescription = "Localized description"
                )
            }
        }
    }
}

@Composable
fun MovieCastHinter(modifier: Modifier = Modifier, cast: List<Person>, onSubmit: ((Int, Int) -> Unit)?) {
    MovieCast(modifier = modifier,
        cast = cast,
        onSubmit = onSubmit)
}
@Composable
fun MovieCastGuesser(modifier: Modifier = Modifier, cast: List<Person>) {
    MovieCast(modifier = modifier, cast = cast)
}