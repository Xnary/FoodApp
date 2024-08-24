package com.example.recipeapp.recipe.detail.view

import com.example.recipeapp.recipe.detail.viewmodel.RecipeDetailViewModelFactory
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.recipeapp.R
import com.example.recipeapp.auth.register.model.UserDatabase
import com.example.recipeapp.auth.register.model.UserRepository
import com.example.recipeapp.databinding.FragmentRecipeDetailBinding
import com.example.recipeapp.recipe.detail.viewmodel.RecipeDetailViewModel
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {
    private val args: RecipeDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentRecipeDetailBinding

    private val viewModel: RecipeDetailViewModel by viewModels {
        val userDao = UserDatabase.getInstance(requireContext()).userDao()
        RecipeDetailViewModelFactory(UserRepository(userDao))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_recipe_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchMealDetails(args.recipeId)

        viewModel.meal.observe(viewLifecycleOwner) { meal ->
            meal?.let {
                binding.recipeTitle.text = it.strMeal
                binding.recipeCategory.text = it.strCategory
                binding.recipeArea.text = it.strArea
                binding.recipeInstructions.text = it.strInstructions
                if (!it.strMealThumb.isNullOrBlank()) {
                    Glide.with(requireContext()).load(it.strMealThumb)
                        .placeholder(R.drawable.ic_launcher_foreground).into(binding.recipeImage)
                }
                viewModel.setupYouTubeVideo(it.strYoutube)
                viewModel.setIngredientsSection(meal)
            }
        }

        binding.favoritesCheckbox.apply {
            setOnCheckedChangeListener { button, isChecked ->
                viewModel.updateFavoriteStatus(
                    isChecked, userId = 1, recipeId = args.recipeId
                ) // Replace `userId = 1` with actual user ID
            }
            lifecycleScope.launch {
                isChecked = viewModel.inFavorites(
                    userId = 1, recipeId = args.recipeId
                ) // Replace `userId = 1` with actual user ID
            }
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.youtubeUrl.observe(viewLifecycleOwner) { url ->
            url?.let { loadYouTubeVideo(it) }
        }

        viewModel.ingredient.observe(viewLifecycleOwner) {
            binding.recipeIngredients.text = it
        }

        binding.showMoreBtn.setOnClickListener {
            val isVisible = binding.constraintLayout.visibility == View.VISIBLE
            binding.constraintLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.showMoreBtn.text =
                getString(if (isVisible) R.string.show_more else R.string.show_less)
        }
    }

    private fun loadYouTubeVideo(url: String) {
        binding.webView.apply {
            webViewClient = WebViewClient() // Ensures the video is loaded within the WebView
            settings.javaScriptEnabled = true
            loadUrl(url)
        }
    }
}